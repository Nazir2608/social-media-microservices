package com.socialmedia.post.storage;

import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(prefix = "media.storage", name = "type", havingValue = "s3")
@Slf4j
public class S3MediaStorageService implements MediaStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String baseUrl;
    private final String folder;

    public S3MediaStorageService(@Value("${media.s3.bucket}") String bucket, @Value("${media.s3.region}") String region, @Value("${media.s3.base-url:}") String baseUrl, @Value("${media.s3.folder:posts}") String folder) {
        this.bucket = bucket;
        this.region = region;
        this.baseUrl = baseUrl;
        this.folder = folder;
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

    @Override
    public String store(String filename, InputStream content) {
        byte[] bytes;
        try {
            bytes = content.readAllBytes();
        } catch (IOException ex) {
            log.error("Failed to read media content for {}", filename, ex);
            throw new IllegalStateException("Failed to read media content", ex);
        }
        String key = folder + "/" + filename;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        String url;
        if (baseUrl != null && !baseUrl.isBlank()) {
            url = baseUrl + "/" + key;
        } else {
            url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        }
        log.debug("Stored media file {} at s3://{}/{}", filename, bucket, key);
        return url;
    }
}

