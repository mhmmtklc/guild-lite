package com.guildlite.security.interceptor;

import com.guildlite.security.dto.UserPrincipal;
import com.guildlite.security.provider.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        logger.debug("WebSocket handshake attempt from: {}", request.getRemoteAddress());

        String token = getTokenFromRequest(request);

        if (token == null || token.trim().isEmpty()) {
            logger.warn("No JWT token provided in WebSocket handshake from: {}", request.getRemoteAddress());
            return false;
        }

        logger.debug("Extracted token (first 20 chars): {}...",
                token.length() > 20 ? token.substring(0, 20) : token);

        try {
            if (jwtTokenProvider.validateToken(token)) {
                UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(token);

                if (userPrincipal.getTeamId() == null) {
                    logger.warn("User {} has no team assignment", userPrincipal.getUsername());
                    return false;
                }

                attributes.put("userPrincipal", userPrincipal);
                logger.info("WebSocket handshake successful for user: {} (team: {})",
                        userPrincipal.getUsername(), userPrincipal.getTeamId());
                return true;
            } else {
                logger.warn("JWT token validation failed");
                return false;
            }
        } catch (Exception e) {
            logger.error("JWT validation error in WebSocket handshake: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String query = request.getURI().getQuery();

        if (query != null && query.contains("token=")) {
            String token = extractTokenFromQuery(query);

            if (token != null && token.startsWith("Bearer ")) {
                logger.debug("Removing 'Bearer ' prefix from token");
                token = token.substring(7).trim();
            }

            return token;
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.debug("Token found in Authorization header");
            return authHeader.substring(7).trim();
        }

        logger.debug("No token found in query params or Authorization header");
        return null;
    }

    private String extractTokenFromQuery(String query) {
        logger.debug("Parsing query string: {}", query);

        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                String token = URLDecoder.decode(param.substring(6), StandardCharsets.UTF_8);
                logger.debug("Found token parameter (length: {})", token.length());
                return token;
            }
        }

        logger.debug("No token parameter found in query string");
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

        if (exception != null) {
            logger.error("WebSocket handshake completed with error: {}", exception.getMessage());
        } else {
            logger.debug("WebSocket handshake completed successfully");
        }
    }
}