package com.chhavi.prodee.journaling.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.journaling.dto.*;
import com.chhavi.prodee.journaling.entity.DailyPixel;
import com.chhavi.prodee.journaling.entity.LogTemplate;
import com.chhavi.prodee.journaling.repository.DailyPixelRepository;
import com.chhavi.prodee.journaling.repository.LogTemplateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PixelJournalService {

    private final LogTemplateRepository templateRepository;
    private final DailyPixelRepository pixelRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ── Templates ────────────────────────────────────────────

    @Transactional
    public LogTemplateResponse createTemplate(String username, LogTemplateRequest request) {
        User user = findUser(username);
        // Validate that colorMapping is valid JSON with integer keys and hex color values
        validateColorMapping(request.colorMapping());

        LogTemplate template = LogTemplate.builder()
                .user(user)
                .name(request.name())
                .colorMapping(request.colorMapping())
                .build();
        return toTemplateResponse(templateRepository.save(template));
    }

    public List<LogTemplateResponse> getTemplates(String username) {
        User user = findUser(username);
        return templateRepository.findByUserId(user.getId())
                .stream().map(this::toTemplateResponse).toList();
    }

    @Transactional
    public LogTemplateResponse updateTemplate(String username, Long templateId, UpdateLogTemplateRequest request) {
        User user = findUser(username);
        LogTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("LogTemplate", "id", templateId));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Template does not belong to you");
        }

        validateColorMapping(request.colorMapping());
        template.setName(request.name());
        template.setColorMapping(request.colorMapping());
        return toTemplateResponse(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(String username, Long templateId) {
        User user = findUser(username);
        LogTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("LogTemplate", "id", templateId));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Template does not belong to you");
        }

        if ("Mood".equalsIgnoreCase(template.getName())) {
            throw new BadRequestException("Default Mood template cannot be deleted");
        }

        long ownedTemplates = templateRepository.findByUserId(user.getId()).size();
        if (ownedTemplates <= 1) {
            throw new BadRequestException("You must keep at least one template");
        }

        templateRepository.delete(template);
    }

    @Transactional
    public void createDefaultMoodTemplateForUser(User user) {
        if (templateRepository.existsByUserIdAndNameIgnoreCase(user.getId(), "Mood")) {
            return;
        }

        LogTemplate template = LogTemplate.builder()
                .user(user)
                .name("Mood")
                .colorMapping("{\"1\":\"#f44336\",\"2\":\"#ff9800\",\"3\":\"#ffc107\",\"4\":\"#8bc34a\",\"5\":\"#4caf50\"}")
                .build();
        templateRepository.save(template);
    }

    // ── Pixels ───────────────────────────────────────────────

    @Transactional
    public DailyPixelResponse paintPixel(String username, DailyPixelRequest request) {
        User user = findUser(username);
        LogTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("LogTemplate", "id", request.templateId()));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Template does not belong to you");
        }

        String color = resolveColorHex(template, request.intensity());

        DailyPixel pixel = pixelRepository
            .findByUserIdAndTemplateIdAndPixelDate(user.getId(), template.getId(), request.date())
            .orElseGet(() -> DailyPixel.builder()
                .user(user)
                .template(template)
                .pixelDate(request.date())
                .build());

        pixel.setIntensity(request.intensity());
        pixel.setPixelValue(request.intensity());
        pixel.setColorHex(color);
        return toPixelResponse(pixelRepository.save(pixel), color);
    }

    public List<DailyPixelResponse> getPixelsForYear(String username, int year) {
        User user = findUser(username);
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return pixelRepository.findByUserIdAndPixelDateBetweenOrderByPixelDateAsc(user.getId(), start, end)
                .stream().map(this::toPixelResponseWithLookup).toList();
    }

    public List<DailyPixelResponse> getPixelsByTemplate(String username, Long templateId) {
        User user = findUser(username);
        return pixelRepository.findByUserIdAndTemplateIdOrderByPixelDateAsc(user.getId(), templateId)
                .stream().map(this::toPixelResponseWithLookup).toList();
    }

    // ── helpers ──────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Validates that the colorMapping is valid JSON with integer string keys
     * and hex color string values. Rejects plain text or malformed JSON.
     */
    private void validateColorMapping(String json) {
        Map<String, String> mapping = parseColorMapping(json);
        if (mapping.isEmpty()) {
            throw new BadRequestException("colorMapping must contain at least one entry");
        }
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            try {
                Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                throw new BadRequestException(
                        "colorMapping keys must be integers. Invalid key: '" + entry.getKey() + "'");
            }
            if (!entry.getValue().matches("^#[0-9a-fA-F]{6}$")) {
                throw new BadRequestException(
                        "colorMapping values must be hex colors (e.g. #ff4500). Invalid value: '" + entry.getValue() + "'");
            }
        }
    }

    private Map<String, String> parseColorMapping(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid colorMapping JSON. Must be a valid JSON object, e.g. {\"1\":\"#ff4500\",\"2\":\"#32cd32\"}. Error: " + e.getMessage());
        }
    }

    private LogTemplateResponse toTemplateResponse(LogTemplate t) {
        return new LogTemplateResponse(t.getId(), t.getName(), t.getColorMapping(), t.getCreatedAt());
    }

    private String resolveColorHex(LogTemplate template, Integer intensity) {
        Map<String, String> mapping = parseColorMapping(template.getColorMapping());
        String intensityKey = String.valueOf(intensity);
        String color = mapping.get(intensityKey);
        if (color == null) {
            throw new BadRequestException("Invalid intensity '" + intensity
                    + "'. Allowed intensity levels: " + mapping.keySet());
        }
        return color;
    }

    /**
     * Build response with resolved colorHex by looking up intensity in template's colorMapping.
     */
    private DailyPixelResponse toPixelResponseWithLookup(DailyPixel p) {
        String colorHex = p.getColorHex();
        if (colorHex == null || colorHex.isBlank()) {
            Map<String, String> mapping = parseColorMapping(p.getTemplate().getColorMapping());
            colorHex = mapping.getOrDefault(String.valueOf(p.getIntensity()), "#808080");
        }
        return new DailyPixelResponse(
                p.getId(), p.getTemplate().getName(), p.getPixelDate(), p.getIntensity(), colorHex);
    }

    private DailyPixelResponse toPixelResponse(DailyPixel p, String resolvedColor) {
        return new DailyPixelResponse(
                p.getId(), p.getTemplate().getName(), p.getPixelDate(), p.getIntensity(), resolvedColor);
    }
}
