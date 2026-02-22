package com.socialmedia.post.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Slf4j
public class LocalMediaStorageService implements MediaStorageService {

    private final Path uploadDir;

    public LocalMediaStorageService(@Value("${media.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            log.error("Could not create upload directory {}", this.uploadDir, ex);
            throw new IllegalStateException("Could not create upload directory", ex);
        }
    }

    @Override
    public String store(String filename, InputStream content) {
        try {
            Path target = uploadDir.resolve(filename);
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            String url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/media/").path(filename).toUriString();
            log.debug("Stored media file {} at {}", filename, target);
            return url;
        } catch (IOException ex) {
            log.error("Could not store media file {}", filename, ex);
            throw new IllegalStateException("Could not store media file", ex);
        }
    }
}
