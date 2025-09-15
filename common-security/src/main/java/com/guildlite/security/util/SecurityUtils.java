package com.guildlite.security.util;

import com.guildlite.security.dto.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;


public final class SecurityUtils {

    private SecurityUtils() {
    }


    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) authentication.getPrincipal());
        }

        return Optional.empty();
    }


    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }


    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getUsername);
    }


    public static Optional<UUID> getCurrentUserTeamId() {
        return getCurrentUser().map(UserPrincipal::getTeamId);
    }


    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserPrincipal;
    }


    public static boolean isUserInTeam(UUID teamId) {
        return getCurrentUserTeamId()
                .map(currentTeamId -> currentTeamId.equals(teamId))
                .orElse(false);
    }


    public static UserPrincipal requireAuthentication() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User must be authenticated"));
    }
}