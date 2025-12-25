package com.paperlesslab.paperless.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {

    @Test
    void ocrQueue_isCreated() {
        RabbitConfig config = new RabbitConfig();
        Queue queue = config.ocrQueue();
        assertThat(queue.getName()).isEqualTo("OCR_QUEUE");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void summaryQueue_isCreated() {
        RabbitConfig config = new RabbitConfig();
        Queue queue = config.resultQueue();
        assertThat(queue.getName()).isEqualTo("RESULT_QUEUE");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void jacksonMessageConverter_isCreated() {
        RabbitConfig config = new RabbitConfig();
        MessageConverter converter = config.jacksonMessageConverter();
        assertThat(converter).isNotNull();
    }
}
