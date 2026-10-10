package com.trading.marketdata_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
/**
 * WebSocket configuration for real-time stock price updates
 * 1. Client connects to /ws endpoint
 * 2. Client subscribes to /topic/prices/{symbol}
 * 3. Server publishes stock price updates to /topic/prices/{symbol}
 * 4. Client receives stock price updates in real time -  so no polling needed
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){

        // Topics prefixed with /topic -> broadcast to all subscribers
        registry.enableSimpleBroker("/topic");

        // Application destination prefix -> for @MessageMapping annotated methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // WebSocket endpoint - clients connect here
        // SockJS fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
