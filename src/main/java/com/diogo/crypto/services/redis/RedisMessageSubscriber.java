package com.diogo.crypto.services.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate template;

    public RedisMessageSubscriber(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
        template.convertAndSend("/topic/updates", receivedMessage);
    }
}
