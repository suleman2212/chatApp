package com.chat.model;

public class ChatMessage {
    private String id;
    private Long userId; // Maps to the auto-incremented ID in your MySQL users table
    private MessageType type;
    private String content;
    private String sender;
    private String timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    public ChatMessage() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
