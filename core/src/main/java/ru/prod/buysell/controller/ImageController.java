package ru.prod.buysell.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.prod.buysell.entity.Image;
import ru.prod.buysell.repository.ImageRepository;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image retrieval endpoints")
public class ImageController {
    private final ImageRepository imageRepository;

    @Operation(summary = "Get image by ID", description = "Retrieve image binary data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image found"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/api/images/{id}")
    public ResponseEntity<?> getImageById(
            @Parameter(description = "Image ID") @PathVariable Long id) {
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(
                image.getOriginalFileName() != null ? image.getOriginalFileName() : "image",
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        return ResponseEntity.ok()
                .header("fileName", encodedFileName)
                .contentType(MediaType.valueOf(image.getContentType()))
                .contentLength(image.getSize())
                .body(new InputStreamResource(new ByteArrayInputStream(image.getBytes())));
    }
}