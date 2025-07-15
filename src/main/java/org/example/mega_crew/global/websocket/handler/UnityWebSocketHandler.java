package org.example.mega_crew.global.websocket.handler;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class UnityWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // client로부터 메시지 수신
        System.out.println("Received: " + message.getPayload());

        // 메시지 응답
        session.sendMessage(new TextMessage("Echo: "+ message.getPayload()));
    }
}
