package com.guildlite.chat.listener;

import com.guildlite.chat.dto.ChatMessage;
import com.guildlite.chat.dto.events.CoinEventsDTO;
import com.guildlite.chat.dto.events.TeamEventsDTO;
import com.guildlite.chat.handler.ChatWebSocketHandler;
import com.guildlite.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatEventListener.class);

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatService chatService;

    public ChatEventListener(ChatWebSocketHandler chatWebSocketHandler, ChatService chatService) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.chatService = chatService;
    }


    @EventListener
    public void handleTeamEvents(TeamEventsDTO event) {
        final ChatMessage.MessageType type = event.getType();
        String action;
        switch (type) {
            case TEAM_JOIN:
                action = "joined";
                break;
            case TEAM_LEAVE:
                action = "left";
                break;
            default:
                return;
        }

        String message = String.format("%s %s the team!", event.getUsername(), action);

        chatService.createSystemMessage(event.getTeamId(), message, type);

        chatWebSocketHandler.broadcastEventToTeam(event.getTeamId(), message);

        log.info("Team event broadcasted: {} {} team {}", event.getUsername(), action, event.getTeamId());
    }

    @EventListener
    public void handleCoinEvents(CoinEventsDTO event) {
        String message = String.format("%s added %d coins to the team pool! Total: %d coins",
                event.getUsername(), event.getAmount(), event.getNewBalance());

        chatService.createSystemMessage(event.getTeamId(), message, event.getType());

        chatWebSocketHandler.broadcastEventToTeam(event.getTeamId(), message);

        log.info("Coin event broadcasted: {} added {} coins to team {}",
                event.getUsername(), event.getAmount(), event.getTeamId());
    }

}