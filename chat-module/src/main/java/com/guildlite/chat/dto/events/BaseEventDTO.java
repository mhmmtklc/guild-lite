package com.guildlite.chat.dto.events;

import com.guildlite.chat.dto.ChatMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEventDTO {

    private String teamId;
    private String userId;
    private String username;

    private ChatMessage.MessageType type;
}
