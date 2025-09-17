package com.guildlite.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guildlite.chat.dto.ChatMessage;
import com.guildlite.chat.service.ChatService;
import com.guildlite.security.dto.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> teamSessions;

    public ChatWebSocketHandler(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
        this.teamSessions = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserPrincipal userPrincipal = getUserPrincipal(session);
        if (userPrincipal == null) {
            log.warn("No user principal found in session attributes");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String teamId = String.valueOf(userPrincipal.getTeamId());
        String userId = String.valueOf(userPrincipal.getId());

        if (teamId == null) {
            log.warn("User {} has no team assignment", userId);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No team assigned"));
            return;
        }

        teamSessions.computeIfAbsent(teamId, k -> new ConcurrentHashMap<>()).put(userId, session);

        log.info("User {} joined team {} chat room", userPrincipal.getUsername(), teamId);

        ChatMessage joinMessage = new ChatMessage(teamId,
                userPrincipal.getUsername() + " joined the chat",
                ChatMessage.MessageType.TEAM_JOIN);

        broadcastToTeam(teamId, joinMessage, userId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        UserPrincipal userPrincipal = getUserPrincipal(session);
        if (userPrincipal == null) {
            return;
        }

        String payload = message.getPayload().toString();

        try {
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

            if (!isValidMessage(chatMessage, userPrincipal)) {
                log.warn("Invalid message from user {}: {}", userPrincipal.getId(), payload);
                return;
            }

            ChatMessage processedMessage = chatService.processMessage(chatMessage, userPrincipal);
            broadcastToTeam(String.valueOf(userPrincipal.getTeamId()), processedMessage, String.valueOf(userPrincipal.getId()));

            log.info("Message from {} to team {}: {}",
                    userPrincipal.getUsername(), userPrincipal.getTeamId(), chatMessage.getMessage());

        } catch (Exception e) {
            log.error("Error processing message from user {}: {}", userPrincipal.getId(), e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error in WebSocket session: {}", exception.getMessage());
        UserPrincipal userPrincipal = getUserPrincipal(session);
        if (userPrincipal != null) {
            removeUserFromTeam(String.valueOf(userPrincipal.getTeamId()), String.valueOf(userPrincipal.getId()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        UserPrincipal userPrincipal = getUserPrincipal(session);
        if (userPrincipal != null) {
            String teamId = String.valueOf(userPrincipal.getTeamId());
            String userId = String.valueOf(userPrincipal.getId());

            removeUserFromTeam(teamId, userId);

            ChatMessage leaveMessage = new ChatMessage(teamId,
                    userPrincipal.getUsername() + " left the chat",
                    ChatMessage.MessageType.TEAM_LEAVE);

            broadcastToTeam(teamId, leaveMessage, userId);

            log.info("User {} left team {} chat room", userPrincipal.getUsername(), teamId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void broadcastToTeam(String teamId, ChatMessage message, String excludeUserId) {
        ConcurrentHashMap<String, WebSocketSession> teamMembers = teamSessions.get(teamId);
        if (teamMembers == null || teamMembers.isEmpty()) {
            return;
        }

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message: {}", e.getMessage());
            return;
        }

        teamMembers.entrySet().removeIf(entry -> {
            String userId = entry.getKey();
            WebSocketSession session = entry.getValue();

            if (excludeUserId != null && excludeUserId.equals(userId)) {
                return false;
            }

            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                    return false;
                }
            } catch (IOException e) {
                log.warn("Failed to send message to user {}: {}", userId, e.getMessage());
            }

            return true;
        });
    }

    public void broadcastEventToTeam(String teamId, String message) {
        ChatMessage eventMessage = new ChatMessage(teamId, message, ChatMessage.MessageType.SYSTEM);
        broadcastToTeam(teamId, eventMessage, null);
    }

    private UserPrincipal getUserPrincipal(WebSocketSession session) {
        return (UserPrincipal) session.getAttributes().get("userPrincipal");
    }

    private boolean isValidMessage(ChatMessage message, UserPrincipal userPrincipal) {
        if (message == null || message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            return false;
        }

        if (message.getMessage().length() > 1000) {
            return false;
        }

        return String.valueOf(userPrincipal.getTeamId()).equals(message.getTeamId());
    }

    private void removeUserFromTeam(String teamId, String userId) {
        if (teamId != null && userId != null) {
            ConcurrentHashMap<String, WebSocketSession> teamMembers = teamSessions.get(teamId);
            if (teamMembers != null) {
                teamMembers.remove(userId);
                if (teamMembers.isEmpty()) {
                    teamSessions.remove(teamId);
                }
            }
        }
    }
}