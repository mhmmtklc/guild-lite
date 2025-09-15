package com.guildlite.security.config;

import com.guildlite.security.interceptor.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketSecurityConfig implements WebSocketConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketSecurityConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // This is a base configuration
        // Individual modules will extend this to register their specific handlers
        // Example usage in other modules:
        // registry.addHandler(chatHandler, "/ws/chat").addInterceptors(jwtHandshakeInterceptor);
    }

    public JwtHandshakeInterceptor getJwtHandshakeInterceptor() {
        return jwtHandshakeInterceptor;
    }
}