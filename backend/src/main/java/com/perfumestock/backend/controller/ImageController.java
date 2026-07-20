package com.perfumestock.backend.controller;

import com.perfumestock.backend.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Product image upload and retrieval")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${app.upload-dir:uploads/products}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private int serverPort;

    @PostMapping("/upload")
    @Operation(summary = "Upload a product image", description = "Uploads an image file (JPEG, PNG, WebP, GIF) up to 5 MB.")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleException("Unsupported file type. Allowed: JPEG, PNG, WebP, GIF");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessRuleException("File too large. Maximum size is 5 MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/api/images/" + filename;
            log.info("Uploaded image: {} ({} bytes)", filename, file.getSize());

            return ResponseEntity.ok(Map.of("url", imageUrl, "filename", filename));
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new BusinessRuleException("Failed to save image: " + e.getMessage());
        }
    }

    @GetMapping("/{filename}")
    @Operation(summary = "Get a product image", description = "Serves an uploaded product image by filename.")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String contentType = detectContentType(filename);
            byte[] data = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }

    private String detectContentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".webp")) return "image/webp";
        if (filename.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }
}
