package com.paperlesslab.paperless.rabbitmq;

import com.paperlesslab.paperless.dto.GenAiResultMessage;
import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenAiResultListener {
    private final DocumentRepository documentRepository;

    @RabbitListener(queues = "OCR_QUEUE")
    public void handleGenAiResult(GenAiResultMessage message) {
        log.info("Received GenAiResultMessage for documentId={}", message.documentId());

        Document doc = documentRepository.findById(message.documentId())
                .orElseThrow(() -> new RuntimeException("Document not found: " + message.documentId()));

        doc.setOcrText(message.ocrText());
        doc.setResult(message.result());
        documentRepository.save(doc);

        log.info("Updated document {} with OCR and result", message.documentId());
    }
}
