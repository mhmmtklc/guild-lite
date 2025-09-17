package com.guildlite.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {

    private String senderId;
    private String senderUsername;
    private String teamId;
    private String message;
    private MessageType type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.CHAT;
    }

    public ChatMessage(String teamId, String message, MessageType type) {
        this();
        this.teamId = teamId;
        this.message = message;
        this.type = type;
        this.senderId = "SYSTEM";
        this.senderUsername = "System";
    }

    public enum MessageType {
        CHAT,
        TEAM_JOIN,
        TEAM_LEAVE,
        COIN_ADD,
        SYSTEM
    }
}