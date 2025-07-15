package org.example.mega_crew.global.websocket.config;

import org.example.mega_crew.global.websocket.handler.UnityWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(new UnityWebSocketHandler(), "/unity-websocket")
                .setAllowedOrigins("*"); // 개발 환경에서만 사용
    }
}
