package com.chat.controller;

import com.chat.model.ChatMessage;
import com.chat.service.ChatHistoryService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
public class ChatController {

    private final ChatHistoryService historyService;

    public ChatController(ChatHistoryService historyService) {
        this.historyService = historyService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                    SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setId(UUID.randomUUID().toString());
        chatMessage.setTimestamp(String.valueOf(System.currentTimeMillis()));
        // Sender comes from the JWT verified at CONNECT time, not the payload --
        // the client can no longer send messages as someone else.
        chatMessage.setSender(verifiedUsername(headerAccessor));
        Long userId = extractUserId(headerAccessor);
        if (userId != null) {
            chatMessage.setUserId(userId);
        }

        if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            historyService.saveMessage(chatMessage);
        }
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setId(UUID.randomUUID().toString());
        chatMessage.setTimestamp(String.valueOf(System.currentTimeMillis()));
        chatMessage.setSender(verifiedUsername(headerAccessor));
        Long userId = extractUserId(headerAccessor);
        if (userId != null) {
            chatMessage.setUserId(userId);
        }
        return chatMessage;
    }

    private Long extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            Object userId = headerAccessor.getSessionAttributes().get("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }

    private String verifiedUsername(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() != null && headerAccessor.getUser().getName() != null) {
            return headerAccessor.getUser().getName();
        }
        if (headerAccessor.getSessionAttributes() != null) {
            Object username = headerAccessor.getSessionAttributes().get("username");
            if (username != null) {
                return username.toString();
            }
        }
        throw new IllegalStateException("No verified username on this session -- CONNECT was not authenticated");
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory() {
        return historyService.getHistory();
    }
}
