package com.chat.config;

import com.chat.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = null;
        Long userId = null;

        if (event.getUser() != null && event.getUser().getName() != null) {
            username = event.getUser().getName();
        }

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            if (username == null && sessionAttributes.containsKey("username")) {
                username = (String) sessionAttributes.get("username");
            }
            if (sessionAttributes.get("userId") instanceof Number number) {
                userId = number.longValue();
            }
        }

        if (username != null) {
            logger.info("User Disconnected successfully: " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(UUID.randomUUID().toString());
            chatMessage.setUserId(userId);
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setContent(username + " left the group chat.");
            chatMessage.setTimestamp(String.valueOf(System.currentTimeMillis()));

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
