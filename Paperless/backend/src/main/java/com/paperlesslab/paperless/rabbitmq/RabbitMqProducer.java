package com.paperlesslab.paperless.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqProducer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDocumentUploaded(DocumentUploadMessage message) {
        log.info("Sending OCR job to queue {}: {}", RabbitConfig.OCR_QUEUE, message);
        rabbitTemplate.convertAndSend(RabbitConfig.OCR_QUEUE, message);
    }
}
