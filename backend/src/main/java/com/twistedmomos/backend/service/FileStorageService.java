package com.twistedmomos.backend.service;

import com.twistedmomos.backend.config.FileStorageProperties;
import com.twistedmomos.backend.exception.InvalidFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Local-disk image storage. Files are served back via the "/uploads/**"
 * static resource mapping — see WebConfig. On Render's filesystem this does
 * NOT survive a redeploy; swapping to S3-compatible object storage later
 * only means changing this one class.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private final FileStorageProperties properties;

    /** Stores the file under {app.upload.dir}/{subfolder}/ and returns the public URL path to serve it from. */
    public String store(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File must be 5MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG, or WEBP images are allowed");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".webp";
        };
        String filename = UUID.randomUUID() + extension;

        try {
            Path targetDir = Path.of(properties.dir(), subfolder);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename).normalize();
            file.transferTo(target);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file", ex);
        }

        return "/uploads/" + subfolder + "/" + filename;
    }
}
