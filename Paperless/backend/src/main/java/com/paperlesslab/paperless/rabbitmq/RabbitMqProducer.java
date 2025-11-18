package com.paperlesslab.paperless.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


@Service
public class RabbitMqProducer {

    private static final String QUEUE = "ocr_queue";
    private static final String RESULT_QUEUE = "result_queue";
    private Channel channel;

    public RabbitMqProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq"); // name aus docker compose!
        factory.setUsername("admin");
        factory.setUsername("admin");

        Connection connection = factory.newConnection();
        factory.setConnectionTimeout(10000);
        this.channel = connection.createChannel();
        channel.queueDeclare(QUEUE, true, false, false, null);

    }

    public void send(Object message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);
        channel.basicPublish("", QUEUE, null, json.getBytes());
        System.out.println(" [Producer] Sent Message: '" + json + "'");
    }





}
