package com.chhavi.prodee.journaling.controller;

import com.chhavi.prodee.common.dto.ApiResponse;
import com.chhavi.prodee.journaling.dto.ScrapbookRequest;
import com.chhavi.prodee.journaling.dto.ScrapbookResponse;
import com.chhavi.prodee.journaling.service.ScrapbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/journal/scrapbook")
@RequiredArgsConstructor
@Validated
@Tag(name = "Scrapbook", description = "Rich-text diary entries with Cloudinary images")
public class ScrapbookController {

    private final ScrapbookService scrapbookService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a scrapbook entry with optional image upload")
    public ResponseEntity<ApiResponse<ScrapbookResponse>> create(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("title") @NotBlank @Size(max = 200) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "placedStickers", required = false) String placedStickers,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ScrapbookRequest request = ScrapbookJsonSupport.requestFrom(title, content, placedStickers);
        ScrapbookResponse entry = scrapbookService.createEntry(user.getUsername(), request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Scrapbook entry created", entry));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a scrapbook entry with optional new image and sticker placements")
    public ResponseEntity<ApiResponse<ScrapbookResponse>> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @RequestParam("title") @NotBlank @Size(max = 200) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "placedStickers", required = false) String placedStickers,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ScrapbookRequest request = ScrapbookJsonSupport.requestFrom(title, content, placedStickers);
        return ResponseEntity.ok(ApiResponse.success(
                "Scrapbook entry updated",
                scrapbookService.updateEntry(user.getUsername(), id, request, image)));
    }

    @GetMapping
    @Operation(summary = "Get all scrapbook entries")
    public ResponseEntity<ApiResponse<List<ScrapbookResponse>>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(scrapbookService.getEntries(user.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single scrapbook entry")
    public ResponseEntity<ApiResponse<ScrapbookResponse>> getById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scrapbookService.getEntry(user.getUsername(), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scrapbook entry")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        scrapbookService.deleteEntry(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Entry deleted", null));
    }
}
