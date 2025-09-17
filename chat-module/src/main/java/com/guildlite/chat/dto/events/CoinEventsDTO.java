package com.guildlite.chat.dto.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoinEventsDTO extends BaseEventDTO {

    private Long amount;
    private Long newBalance;
}
