package com.chhavi.prodee.journaling.service;

import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.journaling.dto.ScrapbookRequest;
import com.chhavi.prodee.journaling.dto.ScrapbookResponse;
import com.chhavi.prodee.journaling.entity.ScrapbookEntry;
import com.chhavi.prodee.journaling.repository.ScrapbookEntryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapbookService {

    private final ScrapbookEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public ScrapbookResponse createEntry(String username, ScrapbookRequest request, MultipartFile image) {
        User user = findUser(username);

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            if (!isCloudinaryConfigured()) {
                log.warn("Cloudinary is not configured (api-key is 'demo'). Skipping image upload.");
                throw new BadRequestException(
                        "Image upload is not available. Cloudinary credentials are not configured. "
                        + "Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
            }
            imageUrl = uploadImage(image);
        }

        ScrapbookEntry entry = ScrapbookEntry.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .imageUrl(imageUrl)
                .build();
        return toResponse(entryRepository.save(entry));
    }

    public List<ScrapbookResponse> getEntries(String username) {
        User user = findUser(username);
        return entryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public ScrapbookResponse getEntry(String username, Long entryId) {
        ScrapbookEntry entry = findEntryForUser(username, entryId);
        return toResponse(entry);
    }

    public void deleteEntry(String username, Long entryId) {
        ScrapbookEntry entry = findEntryForUser(username, entryId);
        entryRepository.delete(entry);
    }

    // ── helpers ──────────────────────────────────────────────

    /**
     * Checks the Cloudinary bean's own config rather than a separate @Value,
     * so there is zero chance of a mismatch.
     */
    private boolean isCloudinaryConfigured() {
        try {
            String apiKey = String.valueOf(cloudinary.config.apiKey);
            return apiKey != null
                    && !apiKey.isBlank()
                    && !"null".equals(apiKey)
                    && !"demo".equalsIgnoreCase(apiKey.trim());
        } catch (Exception e) {
            log.warn("Unable to read Cloudinary config: {}", e.getMessage());
            return false;
        }
    }

    private String uploadImage(MultipartFile image) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(),
                    ObjectUtils.asMap("folder", "prodee/scrapbook"));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new BadRequestException("Image upload failed: " + e.getMessage());
        }
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private ScrapbookEntry findEntryForUser(String username, Long entryId) {
        User user = findUser(username);
        ScrapbookEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("ScrapbookEntry", "id", entryId));
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Entry does not belong to you");
        }
        return entry;
    }

    private ScrapbookResponse toResponse(ScrapbookEntry e) {
        return new ScrapbookResponse(
                e.getId(), e.getTitle(), e.getContent(), e.getImageUrl(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
