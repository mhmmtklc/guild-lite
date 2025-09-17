package com.guildlite.chat.controller;

import com.guildlite.chat.dto.ChatMessage;
import com.guildlite.chat.service.ChatService;
import com.guildlite.security.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Controller", description = "Chat message history controller")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "50") int limit) {

        if (userPrincipal.getTeamId() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessage> history = chatService.getChatHistory(String.valueOf(userPrincipal.getTeamId()), limit);
        return ResponseEntity.ok(history);
    }


    @GetMapping("/stats")
    public ResponseEntity<ChatService.ChatStats> getChatStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal.getTeamId() == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatService.ChatStats stats = chatService.getTeamChatStats(String.valueOf(userPrincipal.getTeamId()));
        return ResponseEntity.ok(stats);
    }


    @DeleteMapping("/history/clear")
    public ResponseEntity<Void> clearChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal.getTeamId() == null) {
            return ResponseEntity.badRequest().build();
        }

        chatService.clearTeamChatHistory(String.valueOf(userPrincipal.getTeamId()));
        return ResponseEntity.ok().build();
    }
}