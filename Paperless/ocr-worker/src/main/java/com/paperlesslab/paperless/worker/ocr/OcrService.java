package com.paperlesslab.paperless.worker.ocr;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.rabbitmq.IndexMessage;
import com.paperlesslab.paperless.worker.rabbitmq.IndexProducer;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.paperlesslab.paperless.rabbitmq.GenAiResultMessage;
import com.paperlesslab.paperless.worker.rabbitmq.ResultProducer;
import com.paperlesslab.paperless.worker.genai.GeminiClient;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final OcrEngine ocrEngine;
    private final GeminiClient geminiClient;
    private final ResultProducer resultProducer;
    private final IndexProducer indexProducer;

    public OcrService(MinioClient minioClient,
                      @Value("${app.minio.bucket}") String bucket,
                      OcrEngine ocrEngine,
                      GeminiClient geminiClient,
                      ResultProducer resultProducer,
                      IndexProducer indexProducer) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.ocrEngine = ocrEngine;
        this.geminiClient = geminiClient;
        this.resultProducer = resultProducer;
        this.indexProducer = indexProducer;
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
                log.info("OCR completed for documentId={}, textLength={}",
                        message.documentId(), text.length());

                String result = null;
                try {
                    result = geminiClient.summarizeGermanBullets(text);
                    log.info("Generated result for documentId={} (length={})",
                            message.documentId(),
                            result == null ? 0 : result.length());
                } catch (Exception e) {
                    log.error("GenAI summarization failed for documentId={}",
                            message.documentId(), e);
                }

                resultProducer.send(new GenAiResultMessage(
                        message.documentId(),
                        text,
                        result
                ));
                log.info("Published GenAiResultMessage for documentId={}",
                        message.documentId());

                indexProducer.send(new IndexMessage(
                        message.documentId(),
                        message.filename(),
                        text,
                        result
                ));

                log.info("Published GenAiResultMessage + IndexMessage for documentId={}",
                        message.documentId());

                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            log.error("Error during OCR processing for document {}",
                    message.documentId(), e);
        }
    }
}
