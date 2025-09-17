package com.guildlite.chat.service;

import com.guildlite.chat.dto.ChatMessage;
import com.guildlite.security.dto.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConcurrentHashMap<String, List<ChatMessage>> chatHistory;

    public ChatService() {
        this.chatHistory = new ConcurrentHashMap<>();
    }

    public ChatMessage processMessage(ChatMessage message, UserPrincipal userPrincipal) {
        message.setSenderId(String.valueOf(userPrincipal.getId()));
        message.setSenderUsername(userPrincipal.getUsername());
        message.setTeamId(String.valueOf(userPrincipal.getTeamId()));
        message.setTimestamp(LocalDateTime.now());
        message.setType(ChatMessage.MessageType.CHAT);


        saveChatMessage(message);

        message = applyMessageFilters(message);

        log.debug("Processed chat message from {} in team {}",
                userPrincipal.getUsername(), userPrincipal.getTeamId());

        return message;
    }

    public List<ChatMessage> getChatHistory(String teamId, int limit) {
        List<ChatMessage> messages = chatHistory.getOrDefault(teamId, new ArrayList<>());

        int size = messages.size();
        if (size <= limit) {
            return new ArrayList<>(messages);
        }

        return new ArrayList<>(messages.subList(size - limit, size));
    }

    public void createSystemMessage(String teamId, String message, ChatMessage.MessageType type) {
        ChatMessage systemMessage = new ChatMessage(teamId, message, type);
        saveChatMessage(systemMessage);
    }

    private void saveChatMessage(ChatMessage message) {
        String teamId = message.getTeamId();

        chatHistory.computeIfAbsent(teamId, k -> new ArrayList<>()).add(message);

        List<ChatMessage> messages = chatHistory.get(teamId);
        if (messages.size() > 100) {
            messages.removeFirst();
        }
    }

    private ChatMessage applyMessageFilters(ChatMessage message) {
        String filteredMessage = message.getMessage()
                .replaceAll("(?i)\\b(badword1|badword2)\\b", "***");

        message.setMessage(filteredMessage);
        return message;
    }

    public void clearTeamChatHistory(String teamId) {
        chatHistory.remove(teamId);
        log.info("Chat history cleared for team: {}", teamId);
    }

    public ChatStats getTeamChatStats(String teamId) {
        List<ChatMessage> messages = chatHistory.getOrDefault(teamId, new ArrayList<>());

        long userMessageCount = messages.stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.CHAT)
                .count();

        long systemMessageCount = messages.size() - userMessageCount;

        return new ChatStats(teamId, messages.size(), userMessageCount, systemMessageCount);
    }


    public record ChatStats(String teamId, int totalMessages, long userMessages, long systemMessages) {
    }

}

