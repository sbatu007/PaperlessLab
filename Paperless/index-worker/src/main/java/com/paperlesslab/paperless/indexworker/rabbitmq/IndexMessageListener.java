package com.paperlesslab.paperless.indexworker.rabbitmq;

import com.paperlesslab.paperless.rabbitmq.IndexMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class IndexMessageListener {

    private static final Logger log = LoggerFactory.getLogger(IndexMessageListener.class);

    @RabbitListener(queues = RabbitConfig.INDEX_QUEUE)
    public void handle(IndexMessage msg) {
        log.info("Received IndexMessage documentId={} filename={} ocrLen={}",
                msg.documentId(),
                msg.filename(),
                msg.ocrText() == null ? 0 : msg.ocrText().length());
    }
}
