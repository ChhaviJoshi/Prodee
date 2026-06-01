package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.journaling.dto.*;
import com.chhavi.prodee.journaling.service.PixelJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal/pixels")
@RequiredArgsConstructor
@Tag(name = "Pixel Journal", description = "Year-in-Pixels dynamic tracking grid")
public class PixelJournalController {

    private final PixelJournalService pixelJournalService;

    // ── Templates ────────────────────────────────────────────

    @PostMapping("/templates")
    @Operation(summary = "Create a custom log template (e.g. Mood with color mapping)")
    public ResponseEntity<ApiResponse<LogTemplateResponse>> createTemplate(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody LogTemplateRequest request) {
        LogTemplateResponse template = pixelJournalService.createTemplate(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", template));
    }

    @GetMapping("/templates")
    @Operation(summary = "Get all your log templates")
    public ResponseEntity<ApiResponse<List<LogTemplateResponse>>> getTemplates(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(pixelJournalService.getTemplates(user.getUsername())));
    }

        @PutMapping("/templates/{templateId}")
        @Operation(summary = "Update an existing template (name and color mapping)")
        public ResponseEntity<ApiResponse<LogTemplateResponse>> updateTemplate(
                        @AuthenticationPrincipal UserDetails user,
                        @PathVariable Long templateId,
                        @Valid @RequestBody UpdateLogTemplateRequest request) {
                LogTemplateResponse updated = pixelJournalService.updateTemplate(user.getUsername(), templateId, request);
                return ResponseEntity.ok(ApiResponse.success("Template updated", updated));
        }

        @DeleteMapping("/templates/{templateId}")
        @Operation(summary = "Delete a custom template and all of its painted pixels")
        public ResponseEntity<ApiResponse<Void>> deleteTemplate(
                        @AuthenticationPrincipal UserDetails user,
                        @PathVariable Long templateId) {
                pixelJournalService.deleteTemplate(user.getUsername(), templateId);
                return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
        }

    // ── Pixels ───────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Paint a pixel for a specific date and template")
    public ResponseEntity<ApiResponse<DailyPixelResponse>> paintPixel(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody DailyPixelRequest request) {
        DailyPixelResponse pixel = pixelJournalService.paintPixel(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pixel painted!", pixel));
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get all pixels for a year")
    public ResponseEntity<ApiResponse<List<DailyPixelResponse>>> getByYear(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable int year) {
        return ResponseEntity.ok(
                ApiResponse.success(pixelJournalService.getPixelsForYear(user.getUsername(), year)));
    }

    @GetMapping("/template/{templateId}")
    @Operation(summary = "Get all pixels for a specific template")
    public ResponseEntity<ApiResponse<List<DailyPixelResponse>>> getByTemplate(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long templateId) {
        return ResponseEntity.ok(
                ApiResponse.success(pixelJournalService.getPixelsByTemplate(user.getUsername(), templateId)));
    }
}
