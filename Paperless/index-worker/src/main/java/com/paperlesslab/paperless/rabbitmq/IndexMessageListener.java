package com.paperlesslab.paperless.rabbitmq;

import com.paperlesslab.paperless.indexworker.elasticsearch.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class IndexMessageListener {

    private static final Logger log = LoggerFactory.getLogger(IndexMessageListener.class);

    private final IndexService indexService;

    public IndexMessageListener(IndexService indexService) {
        this.indexService = indexService;
    }

    @RabbitListener(queues = RabbitConfig.INDEX_QUEUE)
    public void handle(IndexMessage msg) {
        log.info("Received IndexMessage documentId={} filename={} ocrLen={}",
                msg.documentId(),
                msg.filename(),
                msg.ocrText() == null ? 0 : msg.ocrText().length());

        indexService.index(
                msg.documentId(),
                msg.filename(),
                msg.ocrText(),
                msg.result()
        );

        log.info("Indexed documentId={} into Elasticsearch", msg.documentId());
    }
}
