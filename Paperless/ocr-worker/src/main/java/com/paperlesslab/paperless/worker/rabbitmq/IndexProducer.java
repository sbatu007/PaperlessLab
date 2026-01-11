package com.paperlesslab.paperless.worker.rabbitmq;

import com.paperlesslab.paperless.rabbitmq.IndexMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class IndexProducer {

    private static final Logger log = LoggerFactory.getLogger(IndexProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public IndexProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(IndexMessage message) {
        log.info("Sending index message to {} for documentId={} (ocrLen={})",
                RabbitConfig.INDEX_QUEUE,
                message.documentId(),
                message.ocrText() == null ? 0 : message.ocrText().length()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.INDEX_QUEUE, message);
    }
}
