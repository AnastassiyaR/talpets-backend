package com.backend.service;

import com.backend.exception.PhotoStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @TempDir
    Path tempDir;

    private PhotoService photoService;

    private static final String VALID_JPEG_BASE64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAA==";
    private static final String VALID_PNG_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String VALID_WEBP_BASE64 = "data:image/webp;base64,UklGRiQAAABXRUJQVlA4IBgAAAAwAQCdASoBAAEAAwA0JaQAA3AA/vuUAAA=";
    private static final String BASE64_WITHOUT_PREFIX = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAA==";

    @BeforeEach
    void setUp() {
        photoService = new PhotoService();
        ReflectionTestUtils.setField(photoService, "photoDirectory", tempDir.toString());
        photoService.init();
    }

    // INIT TESTS

    @Test
    void init_shouldCreateDirectorySuccessfully() {
        // GIVEN
        PhotoService newService = new PhotoService();
        Path newTempDir = tempDir.resolve("new-upload-dir");
        ReflectionTestUtils.setField(newService, "photoDirectory", newTempDir.toString());

        // WHEN
        assertDoesNotThrow(() -> newService.init());

        // THEN
        assertTrue(Files.exists(newTempDir));
        assertTrue(Files.isDirectory(newTempDir));
    }

    @Test
    void init_shouldNotFailIfDirectoryAlreadyExists() {
        // GIVEN - directory already created in setUp()
        PhotoService newService = new PhotoService();
        ReflectionTestUtils.setField(newService, "photoDirectory", tempDir.toString());

        // WHEN & THEN
        assertDoesNotThrow(() -> newService.init());
        assertTrue(Files.exists(tempDir));
    }

    // SAVE PHOTO TESTS

    @Test
    void savePhoto_shouldSavePngPhotoSuccessfully() throws IOException {
        // WHEN
        String filename = photoService.savePhoto(VALID_PNG_BASE64);

        // THEN
        assertNotNull(filename);
        assertTrue(filename.endsWith(".png"));
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));
        assertTrue(Files.size(savedFile) > 0);
    }

    @Test
    void savePhoto_shouldSaveWebpPhotoSuccessfully() throws IOException {
        // WHEN
        String filename = photoService.savePhoto(VALID_WEBP_BASE64);

        // THEN
        assertNotNull(filename);
        assertTrue(filename.endsWith(".webp"));
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));
        assertTrue(Files.size(savedFile) > 0);
    }

    @Test
    void savePhoto_shouldSavePhotoWithoutDataUriPrefix() throws IOException {
        // WHEN
        String filename = photoService.savePhoto(BASE64_WITHOUT_PREFIX);

        // THEN
        assertNotNull(filename);
        assertTrue(filename.endsWith(".jpg")); // default extension
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));
        assertTrue(Files.size(savedFile) > 0);
    }

    @Test
    void savePhoto_shouldReturnNull_whenPhotoIsNull() {
        // WHEN
        String filename = photoService.savePhoto(null);

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldReturnNull_whenPhotoIsEmpty() {
        // WHEN
        String filename = photoService.savePhoto("");

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldReturnNull_whenPhotoIsBlank() {
        // WHEN
        String filename = photoService.savePhoto("   ");

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldReturnNull_whenBase64IsInvalid() {
        // GIVEN
        String invalidBase64 = "data:image/jpeg;base64,invalid!!!base64";

        // WHEN
        String filename = photoService.savePhoto(invalidBase64);

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldReturnNull_whenFileSizeExceedsLimit() {
        // GIVEN - Create a large base64 string (>5MB)
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        String largeBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(largeBytes);

        // WHEN
        String filename = photoService.savePhoto(largeBase64);

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldReturnNull_whenUnsupportedImageFormat() {
        // GIVEN
        String unsupportedFormat = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

        // WHEN
        String filename = photoService.savePhoto(unsupportedFormat);

        // THEN
        assertNull(filename);
    }

    @Test
    void savePhoto_shouldPreventDirectoryTraversal() {
        // GIVEN - This test verifies the security check happens during filename generation
        // The actual directory traversal attack would be caught by the uploadDir.resolve() check

        // WHEN & THEN
        String filename = photoService.savePhoto(VALID_JPEG_BASE64);
        assertNotNull(filename);
        assertFalse(filename.contains(".."));
        assertFalse(filename.contains("/"));
        assertFalse(filename.contains("\\"));
    }

    // DELETE PHOTO TESTS

    @Test
    void deletePhoto_shouldDeletePhotoSuccessfully() throws IOException {
        // GIVEN
        String filename = photoService.savePhoto(VALID_JPEG_BASE64);
        assertNotNull(filename);
        Path savedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(savedFile));

        // WHEN
        assertDoesNotThrow(() -> photoService.deletePhoto(filename));

        // THEN
        assertFalse(Files.exists(savedFile));
    }

    @Test
    void deletePhoto_shouldNotThrowException_whenFileDoesNotExist() {
        // GIVEN
        String nonExistentFilename = "nonexistent.jpg";

        // WHEN & THEN
        assertDoesNotThrow(() -> photoService.deletePhoto(nonExistentFilename));
    }

    @Test
    void deletePhoto_shouldNotThrowException_whenFilenameIsNull() {
        // WHEN & THEN
        assertDoesNotThrow(() -> photoService.deletePhoto(null));
    }

    @Test
    void deletePhoto_shouldNotThrowException_whenFilenameIsEmpty() {
        // WHEN & THEN
        assertDoesNotThrow(() -> photoService.deletePhoto(""));
    }

    @Test
    void deletePhoto_shouldNotThrowException_whenFilenameIsBlank() {
        // WHEN & THEN
        assertDoesNotThrow(() -> photoService.deletePhoto("   "));
    }

    // GET PHOTO AS BASE64 TESTS

    @Test
    void getPhotoAsBase64_shouldReturnBase64String_whenPhotoExists() {
        // GIVEN
        String filename = photoService.savePhoto(VALID_JPEG_BASE64);
        assertNotNull(filename);

        // WHEN
        String result = photoService.getPhotoAsBase64(filename);

        // THEN
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/jpeg;base64,"));
        assertTrue(result.length() > 50);
    }

    @Test
    void getPhotoAsBase64_shouldReturnCorrectMimeType_forPng() {
        // GIVEN
        String filename = photoService.savePhoto(VALID_PNG_BASE64);
        assertNotNull(filename);

        // WHEN
        String result = photoService.getPhotoAsBase64(filename);

        // THEN
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void getPhotoAsBase64_shouldReturnCorrectMimeType_forWebp() {
        // GIVEN
        String filename = photoService.savePhoto(VALID_WEBP_BASE64);
        assertNotNull(filename);

        // WHEN
        String result = photoService.getPhotoAsBase64(filename);

        // THEN
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/webp;base64,"));
    }

    @Test
    void getPhotoAsBase64_shouldReturnNull_whenPhotoDoesNotExist() {
        // GIVEN
        String nonExistentFilename = "nonexistent.jpg";

        // WHEN
        String result = photoService.getPhotoAsBase64(nonExistentFilename);

        // THEN
        assertNull(result);
    }

    @Test
    void getPhotoAsBase64_shouldReturnNull_whenFilenameIsNull() {
        // WHEN
        String result = photoService.getPhotoAsBase64(null);

        // THEN
        assertNull(result);
    }

    @Test
    void getPhotoAsBase64_shouldReturnNull_whenFilenameIsEmpty() {
        // WHEN
        String result = photoService.getPhotoAsBase64("");

        // THEN
        assertNull(result);
    }

    @Test
    void getPhotoAsBase64_shouldReturnNull_whenFilenameIsBlank() {
        // WHEN
        String result = photoService.getPhotoAsBase64("   ");

        // THEN
        assertNull(result);
    }

    // CLEAN UPLOAD FOLDER TESTS

    @Test
    void cleanUploadFolder_shouldDeleteAllFiles() throws IOException {
        // GIVEN
        String filename1 = photoService.savePhoto(VALID_JPEG_BASE64);
        String filename2 = photoService.savePhoto(VALID_PNG_BASE64);
        String filename3 = photoService.savePhoto(VALID_WEBP_BASE64);

        assertNotNull(filename1);
        assertNotNull(filename2);
        assertNotNull(filename3);

        assertTrue(Files.exists(tempDir.resolve(filename1)));
        assertTrue(Files.exists(tempDir.resolve(filename2)));
        assertTrue(Files.exists(tempDir.resolve(filename3)));

        // WHEN
        photoService.cleanUploadFolder();

        // THEN
        assertFalse(Files.exists(tempDir.resolve(filename1)));
        assertFalse(Files.exists(tempDir.resolve(filename2)));
        assertFalse(Files.exists(tempDir.resolve(filename3)));
    }

    @Test
    void cleanUploadFolder_shouldNotThrowException_whenFolderIsEmpty() {
        // WHEN & THEN
        assertDoesNotThrow(() -> photoService.cleanUploadFolder());
    }

    // INTEGRATION TESTS

    @Test
    void saveAndRetrievePhoto_shouldReturnSameContent() {
        // GIVEN
        String originalBase64 = VALID_JPEG_BASE64;

        // WHEN
        String filename = photoService.savePhoto(originalBase64);
        String retrievedBase64 = photoService.getPhotoAsBase64(filename);

        // THEN
        assertNotNull(filename);
        assertNotNull(retrievedBase64);
        assertTrue(retrievedBase64.startsWith("data:image/jpeg;base64,"));

        // Extract base64 content without prefix
        String originalContent = originalBase64.split(",")[1];
        String retrievedContent = retrievedBase64.split(",")[1];

        assertEquals(originalContent, retrievedContent);
    }

    @Test
    void saveUpdateAndDeletePhoto_shouldWorkCorrectly() throws IOException {
        // GIVEN - Save first photo
        String filename1 = photoService.savePhoto(VALID_JPEG_BASE64);
        assertNotNull(filename1);
        assertTrue(Files.exists(tempDir.resolve(filename1)));

        // WHEN - Delete old photo and save new one
        photoService.deletePhoto(filename1);
        String filename2 = photoService.savePhoto(VALID_PNG_BASE64);

        // THEN
        assertFalse(Files.exists(tempDir.resolve(filename1)));
        assertNotNull(filename2);
        assertTrue(Files.exists(tempDir.resolve(filename2)));
        assertNotEquals(filename1, filename2);
    }

    @Test
    void generateUniqueFilenames_shouldNotCollide() {
        // WHEN - Save multiple photos
        String filename1 = photoService.savePhoto(VALID_JPEG_BASE64);
        String filename2 = photoService.savePhoto(VALID_JPEG_BASE64);
        String filename3 = photoService.savePhoto(VALID_JPEG_BASE64);

        // THEN - All filenames should be unique
        assertNotNull(filename1);
        assertNotNull(filename2);
        assertNotNull(filename3);
        assertNotEquals(filename1, filename2);
        assertNotEquals(filename2, filename3);
        assertNotEquals(filename1, filename3);
    }
}