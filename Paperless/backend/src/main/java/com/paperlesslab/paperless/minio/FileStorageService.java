package com.paperlesslab.paperless.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final MinioClient minioClient;
    private final String bucket;

    public FileStorageService(MinioClient minioClient,
                              @Value("${app.minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public String save(String objectName, InputStream inputStream, long size, String contentType) {
        try {
            ensureBucketExists();

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            minioClient.putObject(args);
            log.info("Stored file '{}' in MinIO bucket '{}'", objectName, bucket);

            return "minio://" + bucket + "/" + objectName;
        } catch (MinioException e) {
            log.error("MinIO error while storing '{}'", objectName, e);
            throw new RuntimeException("MinIO error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Could not store file '{}' in MinIO", objectName, e);
            throw new RuntimeException("Failed to store file in MinIO", e);
        }
    }
    public void uploadPdf(MultipartFile file, String objectName) {
        try (InputStream is = file.getInputStream()) {
            save(objectName, is, file.getSize(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build()
            );
            log.info("Created MinIO bucket '{}'", bucket);
        }
    }
}
