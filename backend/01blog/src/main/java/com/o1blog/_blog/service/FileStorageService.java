package com.o1blog._blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final String TEMP_DIR = "temp";
    private final String PERMANENT_DIR = "posts";
    private final String AVATAR_DIR = "avatars";

    // ─── Whitelists ───────────────────────────────────────────────────────────

    /** Allowed extensions for avatar uploads (images only). */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp");

    /** Allowed extensions for post banners / temp uploads (images + videos). */
    private static final Set<String> ALLOWED_MEDIA_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "mp4", "mov", "avi", "webm", "mkv");

    /** Allowed MIME types for images. */
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    /** Allowed MIME types for images + videos. */
    private static final Set<String> ALLOWED_MEDIA_MIME = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/quicktime", "video/x-msvideo",
            "video/webm", "video/x-matroska");

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Save a temp upload (images or video used in post editor). */
    public String saveTemp(MultipartFile file) {
        validateFile(file, ALLOWED_MEDIA_EXTENSIONS, ALLOWED_MEDIA_MIME);
        return store(file, TEMP_DIR);
    }

    /** Save an avatar upload (images only). */
    public String saveAvatar(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_EXTENSIONS, ALLOWED_IMAGE_MIME);
        String filename = store(file, AVATAR_DIR);
        return AVATAR_DIR + "/" + filename;
    }

    /** Save a post banner permanently (images or video). */
    public String save(MultipartFile file) {
        validateFile(file, ALLOWED_MEDIA_EXTENSIONS, ALLOWED_MEDIA_MIME);
        String filename = store(file, PERMANENT_DIR);
        return PERMANENT_DIR + "/" + filename;
    }

    /** Move a validated temp file to permanent storage. */
    public String moveTempToPermanent(String tempFilename) {
        // Guard against path traversal in the filename received from the client
        String safeName = Paths.get(tempFilename).getFileName().toString();

        try {
            Path tempPath = Paths.get(uploadDir, TEMP_DIR).resolve(safeName).normalize();
            assertWithinBase(tempPath, Paths.get(uploadDir, TEMP_DIR));

            if (!Files.exists(tempPath)) {
                // Already moved or never in temp — return permanent path as-is
                return PERMANENT_DIR + "/" + safeName;
            }

            Path permanentDir = Paths.get(uploadDir, PERMANENT_DIR);
            Files.createDirectories(permanentDir);

            Path newPath = permanentDir.resolve(safeName).normalize();
            assertWithinBase(newPath, permanentDir);

            Files.move(tempPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return PERMANENT_DIR + "/" + safeName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to permanent storage", e);
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    /**
     * Validates the uploaded file against:
     * 1. Non-empty check
     * 2. Whitelisted file extension
     * 3. Whitelisted MIME type declared by the client
     * 4. Magic-bytes check (actual file content)
     */
    private void validateFile(MultipartFile file,
            Set<String> allowedExtensions,
            Set<String> allowedMimeTypes) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty.");
        }

        // 1. Extension check
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File has no extension.");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!allowedExtensions.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "File type '." + ext + "' is not allowed.");
        }

        // 2. Client-declared MIME type check
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "MIME type '" + contentType + "' is not allowed.");
        }

        // 3. Magic-bytes check — read the first bytes of the actual content
        // so an attacker cannot simply rename a .jsp to .jpg
        try (InputStream is = file.getInputStream()) {
            byte[] magic = is.readNBytes(12);
            if (!isAllowedMagicBytes(magic, ext)) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "File content does not match declared type.");
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not read file content for validation.");
        }
    }

    /**
     * Checks the first bytes of file content (magic numbers) to verify the true
     * file type regardless of extension or declared MIME type.
     */
    private boolean isAllowedMagicBytes(byte[] bytes, String ext) {
        if (bytes.length < 4)
            return false;

        int b0 = bytes[0] & 0xFF;
        int b1 = bytes[1] & 0xFF;
        int b2 = bytes[2] & 0xFF;
        int b3 = bytes[3] & 0xFF;

        // JPEG: FF D8 FF
        if (b0 == 0xFF && b1 == 0xD8 && b2 == 0xFF)
            return true;

        // PNG: 89 50 4E 47
        if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47)
            return true;

        // GIF: 47 49 46 38
        if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46 && b3 == 0x38)
            return true;

        // WebP: RIFF....WEBP (bytes 0-3 = RIFF, bytes 8-11 = WEBP)
        if (b0 == 0x52 && b1 == 0x49 && b2 == 0x46 && b3 == 0x46 &&
                bytes.length >= 12 &&
                (bytes[8] & 0xFF) == 0x57 && (bytes[9] & 0xFF) == 0x45 &&
                (bytes[10] & 0xFF) == 0x42 && (bytes[11] & 0xFF) == 0x50) {
            return true;
        }

        // MP4 / MOV: ftyp at bytes 4-7
        if (bytes.length >= 8) {
            int b4 = bytes[4] & 0xFF;
            int b5 = bytes[5] & 0xFF;
            int b6 = bytes[6] & 0xFF;
            int b7 = bytes[7] & 0xFF;
            if (b4 == 0x66 && b5 == 0x74 && b6 == 0x79 && b7 == 0x70)
                return true;
        }

        // AVI: RIFF....AVI
        if (b0 == 0x52 && b1 == 0x49 && b2 == 0x46 && b3 == 0x46 &&
                bytes.length >= 12 &&
                (bytes[8] & 0xFF) == 0x41 && (bytes[9] & 0xFF) == 0x56 &&
                (bytes[10] & 0xFF) == 0x49 && (bytes[11] & 0xFF) == 0x20) {
            return true;
        }

        // WebM / MKV: 1A 45 DF A3
        if (b0 == 0x1A && b1 == 0x45 && b2 == 0xDF && b3 == 0xA3)
            return true;

        return false;
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    /**
     * Saves the file to the given sub-directory and returns only the filename.
     * Strips path components from the original name and generates a UUID prefix
     * to avoid collisions and predictable names.
     */
    private String store(MultipartFile file, String subDir) {
        try {
            // Strip any path components from the original filename (path traversal guard)
            String originalName = Paths.get(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "file").getFileName().toString();

            // Sanitise: keep only safe characters
            String safeName = originalName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

            // UUID prefix prevents enumeration and collisions
            String filename = UUID.randomUUID() + "_" + safeName;

            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);

            Path filePath = dir.resolve(filename).normalize();
            // Ensure resolved path is still inside the intended directory
            assertWithinBase(filePath, dir);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filename;

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in '" + subDir + "'", e);
        }
    }

    /**
     * Prevents path traversal attacks by asserting that {@code path} resolves
     * inside {@code base}. Throws 400 if the resolved path escapes the base.
     */
    private void assertWithinBase(Path path, Path base) {
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(normalizedBase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file path detected.");
        }
    }
}