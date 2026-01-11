package com.paperlesslab.paperless.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

class RabbitMqProducerTest {

    @Test
    void sendIndexMessage_sendsToIndexQueue() {
        RabbitTemplate tpl = mock(RabbitTemplate.class);
        RabbitMqProducer producer = new RabbitMqProducer(tpl);

        IndexMessage msg = new IndexMessage(1L, "Hello.pdf", "Hello", "Summary");
        producer.sendIndexMessage(msg);

        verify(tpl).convertAndSend(RabbitConfig.INDEX_QUEUE, msg);
    }
}
