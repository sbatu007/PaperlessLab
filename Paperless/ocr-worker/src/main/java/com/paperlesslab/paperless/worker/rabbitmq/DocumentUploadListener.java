package com.paperlesslab.paperless.worker.rabbitmq;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DocumentUploadListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadListener.class);

    @RabbitListener(queues = RabbitConfig.OCR_QUEUE)
    public void handle(DocumentUploadMessage message) {
        log.info("Received OCR job: documentId={}, filename={}, description={}",
                message.documentId(), message.filename(), message.description());
    }
}