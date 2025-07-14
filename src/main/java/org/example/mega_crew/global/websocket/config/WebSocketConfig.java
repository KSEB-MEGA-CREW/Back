package org.example.mega_crew.global.websocket.config;

import org.example.mega_crew.global.websocket.handler.MyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer  {
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new MyWebSocketHandler(),"/ws")
        .setAllowedOrigins("*"); // 개발 환경에서는 모두 허용, 실제 서비스에서는 도메인 제한
  }
}
