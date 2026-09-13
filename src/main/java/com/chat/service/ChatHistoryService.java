package com.chat.service;

import com.chat.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryService.class);
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private static final String CHAT_HISTORY_KEY = "chat:history:public";
    private static final int MAX_HISTORY = 100;
    private final List<ChatMessage> fallbackHistory = new CopyOnWriteArrayList<>();

    public ChatHistoryService(RedisTemplate<String, ChatMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveMessage(ChatMessage message) {
        try {
            redisTemplate.opsForList().rightPush(CHAT_HISTORY_KEY, message);
            redisTemplate.opsForList().trim(CHAT_HISTORY_KEY, -MAX_HISTORY, -1);
        } catch (Exception e) {
            logger.warn("Redis unavailable for saving message, using in-memory store: {}", e.getMessage());
            fallbackHistory.add(message);
            if (fallbackHistory.size() > MAX_HISTORY) {
                fallbackHistory.remove(0);
            }
        }
    }

    public List<ChatMessage> getHistory() {
        try {
            List<ChatMessage> list = redisTemplate.opsForList().range(CHAT_HISTORY_KEY, 0, -1);
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable for retrieving history, using in-memory store: {}", e.getMessage());
        }
        return new ArrayList<>(fallbackHistory);
    }
}
