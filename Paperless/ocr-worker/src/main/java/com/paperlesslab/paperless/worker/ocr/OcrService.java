package com.paperlesslab.paperless.worker.ocr;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final OcrEngine ocrEngine;

    public OcrService(MinioClient minioClient,
                      @Value("${app.minio.bucket}") String bucket,
                      OcrEngine ocrEngine) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.ocrEngine = ocrEngine;
    }

    public void process(DocumentUploadMessage message) {
        String objectName = message.filename();

        try {
            log.info("Starting OCR for documentId={}, objectName={}",
                    message.documentId(), objectName);

            try (InputStream in = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            )) {
                Path tempFile = Files.createTempFile("paperless-ocr-", ".pdf");
                Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String text = ocrEngine.extractText(tempFile);
                log.info("OCR result for document {}: {}", message.documentId(), text);

                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            log.error("Error during OCR processing for document {}", message.documentId(), e);
        }
    }
}
