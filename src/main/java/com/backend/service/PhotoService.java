package com.backend.service;


import com.backend.exception.PhotoStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final String DEFAULT_EXTENSION = ".jpg";

    private static final String DIR_TRAVERSAL_MSG = "Attempted directory traversal attack with filename: {}";

    @Value("${app.upload.photo-dir}")
    private String photoDirectory;

    private Path uploadDir;


    /**
     * Initializes the upload directory on service startup.
     * Creates the directory if it doesn't exist.
     */
    @PostConstruct
    public void init() {
        try {
            uploadDir = Paths.get(photoDirectory);
            Files.createDirectories(uploadDir);
            log.info("Photo upload directory initialized: {}", uploadDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", photoDirectory, e);
            throw new IllegalStateException("Could not create upload directory", e);
        }
    }

    /**
     * Saves a photo from base64 encoded string to the file system.
     *
     * @param base64Photo Base64 encoded photo string (with or without data URI prefix)
     * @return Generated filename if successful, null otherwise
     */
    public String savePhoto(String base64Photo) {
        if (base64Photo == null || base64Photo.isBlank()) {
            log.warn("Attempted to save null or empty photo");
            return null;
        }

        try {
            // Extract base64 data (remove data URI prefix if present)
            String base64Data = extractBase64Data(base64Photo);
            byte[] bytes = Base64.getDecoder().decode(base64Data);

            // Validate file size
            if (bytes.length > MAX_FILE_SIZE) {
                log.error("Photo size exceeds maximum allowed size: {} bytes", bytes.length);
                throw new IllegalArgumentException("Photo size exceeds 5MB limit");
            }

            // Determine file extension from data URI or use default
            String extension = extractExtension(base64Photo);
            String filename = generateFilename(extension);
            Path filePath = uploadDir.resolve(filename);

            // Prevent directory traversal attacks
            if (!filePath.startsWith(uploadDir)) {
                log.error(DIR_TRAVERSAL_MSG, filename);
                throw new SecurityException("Invalid filename");
            }

            Files.write(filePath, bytes);
            log.info("Photo saved successfully: {} ({} bytes)", filename, bytes.length);
            return filename;

        } catch (Exception e) {
            log.error("Error saving photo", e);
            return null;
        }
    }

    /**
     * Deletes a photo from the file system.
     *
     * @param filename Name of the file to delete
     */
    public void deletePhoto(String filename) {
        if (filename == null || filename.isBlank()) {
            log.debug("Attempted to delete null or empty filename");
            return;
        }

        try {
            Path filePath = uploadDir.resolve(filename);

            // Prevent directory traversal attacks
            if (!filePath.startsWith(uploadDir)) {
                log.error(DIR_TRAVERSAL_MSG, filename);
                throw new SecurityException("Invalid filename");
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Photo deleted successfully: {}", filename);
            } else {
                log.warn("Photo not found for deletion: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete photo: {}", filename, e);
            throw new PhotoStorageException("Failed to delete photo");
        }
    }

    /**
     * Retrieves a photo as a base64 encoded string with data URI prefix.
     *
     * @param filename Name of the file to retrieve
     * @return Base64 encoded photo with data URI prefix, or null if file doesn't exist
     */
    public String getPhotoAsBase64(String filename) {
        if (filename == null || filename.isBlank()) {
            log.debug("Attempted to retrieve photo with null or empty filename");
            return null;
        }

        try {
            Path filePath = uploadDir.resolve(filename);

            // Prevent directory traversal attacks
            if (!filePath.startsWith(uploadDir)) {
                log.error(DIR_TRAVERSAL_MSG, filename);
                throw new SecurityException("Invalid filename");
            }

            if (!Files.exists(filePath)) {
                log.warn("Photo not found: {}", filename);
                return null;
            }

            byte[] bytes = Files.readAllBytes(filePath);
            String mimeType = determineMimeType(filename);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            log.debug("Photo retrieved successfully: {} ({} bytes)", filename, bytes.length);
            return String.format("data:%s;base64,%s", mimeType, base64);
        } catch (IOException e) {
            log.error("Error reading photo: {}", filename, e);
            return null;
        }
    }

    /**
     * Extracts base64 data from a string, removing data URI prefix if present.
     *
     * @param base64Photo Photo string that may contain data URI prefix
     * @return Pure base64 encoded data
     */
    private String extractBase64Data(String base64Photo) {
        return base64Photo.contains(",")
                ? base64Photo.split(",")[1]
                : base64Photo;
    }

    /**
     * Extracts file extension from data URI or returns default.
     *
     * @param base64Photo Photo string with potential data URI prefix
     * @return File extension (e.g., ".jpg", ".png")
     */
    private String extractExtension(String base64Photo) {
        if (base64Photo.startsWith("data:image/")) {
            String mimeType = base64Photo.substring(11, base64Photo.indexOf(';'));
            String ext = "." + mimeType;

            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                log.error("Unsupported image format: {}", ext);
                throw new SecurityException("Unsupported image format: " + ext);
            }

            return ext;
        }

        // If no data URI prefix, assume default extension
        return DEFAULT_EXTENSION;
    }

    /**
     * Generates a unique filename with the given extension.
     *
     * @param extension File extension to append
     * @return Unique filename
     */
    private String generateFilename(String extension) {
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Determines MIME type based on file extension.
     *
     * @param filename Name of the file
     * @return MIME type string
     */
    private String determineMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg"; // default for .jpg, .jpeg
    }

    public void cleanUploadFolder() {
        try {
            if (Files.exists(uploadDir)) {
                Files.list(uploadDir)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete file: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Failed to clean upload folder", e);
        }
    }
}
