package com.paperlesslab.paperless.rabbitmq;

import com.paperlesslab.paperless.dto.GenAiResultMessage;
import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class GenAiResultListener {

    private static final Logger log = LoggerFactory.getLogger(GenAiResultListener.class);

    private final DocumentRepository documentRepository;

    public GenAiResultListener(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @RabbitListener(queues = RabbitConfig.RESULT_QUEUE)
    public void handleOcrResult(GenAiResultMessage message) {
        log.info("Received OCR result for document ID: {}", message.documentId());

        Document doc = documentRepository.findById(message.documentId())
                .orElseThrow(() -> new RuntimeException("Document not found: " + message.documentId()));

        doc.setOcrText(message.ocrText());
        doc.setResult(message.result());

        documentRepository.save(doc);

        log.info("Saved OCR text and result for document ID: {}", message.documentId());
    }
}
