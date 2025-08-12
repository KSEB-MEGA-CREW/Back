package org.example.mega_crew.domain.text.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.text.config.TranslationAiServerConfig;
import org.example.mega_crew.domain.text.dto.TextTranslationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTranslationProxyService {
    private final RestTemplate restTemplate;
    private final TranslationAiServerConfig aiServerConfig;

    @Async
    public void submitTranslationAsync(TextTranslationRequest request, String requestId){
        long proxyStartTime = System.currentTimeMillis();

        try{
            log.info("AI 서버로 번역 요청 전송 시작: requestId={}, sessionId={}, textLength={}",
                    requestId, request.getSessionId(), request.getText().length());

            // 1. 요청 준비
            String url = aiServerConfig.getBaseUrl() + aiServerConfig.getEndpoint();
            HttpHeaders headers = createHttpHeaders(requestId);
            Map<String, Object> requestBody = createRequestBody(request, requestId);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 2. AI 서버로 Fire-and-Forget 방식 전송
            restTemplate.postForEntity(url, requestEntity, Void.class);

            long proxyEndtime = System.currentTimeMillis();
            long totalProxyTime = proxyEndtime - proxyStartTime;

            log.info("AI 서버로 번역 요청 전송 완료: requestId={}, proxyTime={}ms",
                    requestId, totalProxyTime);
        } catch (Exception e){
            long totalErrorTime = System.currentTimeMillis() - proxyStartTime;

            log.error("AI 서버 번역 요청 전송 실패: requestId={}, errorTime={}ms, error={}",
                    requestId, totalErrorTime, e.getMessage(), e);
        }
    }

    private HttpHeaders createHttpHeaders(String requestId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Request-Id", requestId);
        headers.set("X-Processing-Mode", "websocket-response");
        headers.set("X-Proxy-Timestamp", String.valueOf(System.currentTimeMillis()));
        headers.set("X-Timeout", String.valueOf(aiServerConfig.getTimeout()));
        return headers;
    }

    private Map<String, Object> createRequestBody(TextTranslationRequest request, String requestId){
        return Map.of(
                "request_id", requestId,
                "text", request.getText(),
                "session_id", request.getSessionId(),
                "timestamp", request.getTimestamp(),
                "user_id", request.getUserId(),
                "response_mode", "websocket" // AI 서버에게 WebSocket을 통해 프론트엔드로 응답하라고 지시
        );
    }
}
