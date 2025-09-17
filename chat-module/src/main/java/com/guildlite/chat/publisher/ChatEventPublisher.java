package com.guildlite.chat.publisher;

import com.guildlite.chat.dto.events.CoinEventsDTO;
import com.guildlite.chat.dto.events.TeamEventsDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public ChatEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishTeamEvents(TeamEventsDTO event) {
        eventPublisher.publishEvent(event);
    }

    public void publishCoinEvents(CoinEventsDTO event) {
        eventPublisher.publishEvent(event);
    }

}