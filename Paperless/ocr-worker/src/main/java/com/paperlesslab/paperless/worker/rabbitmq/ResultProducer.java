package com.paperlesslab.paperless.worker.rabbitmq;

import com.paperlesslab.paperless.rabbitmq.GenAiResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResultProducer {

    private static final Logger log = LoggerFactory.getLogger(ResultProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public ResultProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(GenAiResultMessage message) {
        log.info("Sending result to {} for documentId={} (summaryLen={}, ocrLen={})",
                RabbitConfig.RESULT_QUEUE,
                message.documentId(),
                message.result() == null ? 0 : message.result().length(),
                message.ocrText() == null ? 0 : message.ocrText().length());

        rabbitTemplate.convertAndSend(RabbitConfig.RESULT_QUEUE, message);
    }
}
