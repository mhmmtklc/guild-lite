package com.guildlite.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrincipal {

    private UUID id;
    private String username;
    private UUID teamId;


    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", teamId=" + teamId +
                '}';
    }
}