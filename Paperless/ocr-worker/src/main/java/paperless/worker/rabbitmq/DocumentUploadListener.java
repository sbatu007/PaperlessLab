package paperless.worker.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DocumentUploadListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadListener.class);

    @RabbitListener(queues = RabbitConfig.OCR_QUEUE)
    public void handle(DocumentUploadMessage message) {
        log.info("OCR-Worker received document: id={}, filename={}",
                message.documentId(), message.filename());

    }
}