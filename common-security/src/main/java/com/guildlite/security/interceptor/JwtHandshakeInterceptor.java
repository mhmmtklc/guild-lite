package com.guildlite.security.interceptor;

import com.guildlite.security.provider.JwtTokenProvider;
import com.guildlite.security.dto.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private static final String TOKEN_PARAM = "token";
    private static final String USER_PRINCIPAL_ATTRIBUTE = "userPrincipal";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        try {
            String token = extractTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                logger.warn("WebSocket handshake failed: No token provided");
                return false;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("WebSocket handshake failed: Invalid token");
                return false;
            }

            UserPrincipal userPrincipal = jwtTokenProvider.getUserPrincipalFromToken(token);
            attributes.put(USER_PRINCIPAL_ATTRIBUTE, userPrincipal);

            logger.info("WebSocket handshake successful for user: {}", userPrincipal.getUsername());
            return true;

        } catch (Exception ex) {
            logger.error("WebSocket handshake error", ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            logger.error("WebSocket handshake completed with error", exception);
        }
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        URI uri = request.getURI();

        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(TOKEN_PARAM);

        if (StringUtils.hasText(token)) {
            return token;
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}