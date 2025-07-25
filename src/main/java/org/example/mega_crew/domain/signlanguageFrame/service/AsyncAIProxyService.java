package org.example.mega_crew.domain.signlanguageFrame.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguageFrame.config.AIServerConfig;
import org.example.mega_crew.domain.signlanguageFrame.dto.FrameRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAIProxyService {
    private final RestTemplate restTemplate;
    private final AIServerConfig aiServerConfig;

    @Async
    public void submitFrameAsync(FrameRequest request, String requestId){
        try{
            String url = aiServerConfig.getUrl() + aiServerConfig.getEndpoint();

            HttpHeaders headers = createHttpHeaders(requestId);
            Map<String, Object> requestBody = createRequestBody(request, requestId);

            HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("AI 서버 비동기 전송 시작: requestId={}, sessionId={}, frameIndex={}",
                    requestId,request.getSessionId(),request.getFrameIndex());
            
            // AI 서버로 비동기 전송 (응답은 별도 처리)
            restTemplate.postForEntity(url, entity, Map.class);
            
            log.info("AI 서버 전송 완료: requestId={}",requestId);
        } catch (Exception ex){
            log.error("AI 서버 전송 실패: requestId={}, error={}", requestId, ex.getMessage(),ex);
            // 추후 필요 시 실패 처리 추가하기
        }
    }

    private HttpHeaders createHttpHeaders(String requestId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Request-Id", requestId);
        headers.set("X-Frame-Format", "jpeg");
        headers.set("X-Processing-Mode", "async");
        return headers;
    }
    
    private Map<String, Object> createRequestBody(FrameRequest request, String requestId) {
        return Map.of(
                "request_id", requestId,
                "frame_data", request.getFrameData(),
                "session_id", request.getSessionId(),
                "frame_index", request.getFrameIndex(),
                "timestamp", request.getTimestamp(),
                "user_id",request.getUserId()
        );
    }
}
