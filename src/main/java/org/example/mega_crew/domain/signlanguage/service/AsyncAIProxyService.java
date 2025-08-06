package org.example.mega_crew.domain.signlanguage.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguage.config.AIServerConfig;
import org.example.mega_crew.domain.signlanguage.dto.FrameRequest;
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
        long proxyStartTime = System.currentTimeMillis();

        try{
            // 1. 요청 준비 시간 측정
            long prepareStartTime = System.currentTimeMillis();

            String url = aiServerConfig.getUrl() + aiServerConfig.getEndpoint();
            HttpHeaders headers = createHttpHeaders(requestId);
            Map<String, Object> requestBody = createRequestBody(request, requestId);
            HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);

            long prepareEndTime = System.currentTimeMillis();

            log.info("프록시 요청 준비 완료: requestId={}, sessionId={}, frameIndex={}, prepareTime={}ms",
                    requestId, request.getSessionId(), request.getFrameIndex(),
                    (prepareEndTime - prepareStartTime));

            // 2. AI 서버 전송 시간 측정
            long aiRequestStartTime = System.currentTimeMillis();

            var response = restTemplate.postForEntity(url, entity, Map.class);

            long aiRequestEndTime = System.currentTimeMillis();
            long totalProxyTime = aiRequestEndTime - proxyStartTime;
            long aiResponseTime = aiRequestEndTime - aiRequestStartTime;

            log.info("프록시 처리 완료: requestId={}, aiResponseTime={}ms, totalProxyTime={}ms, httpStatus={}",
                    requestId, aiResponseTime, totalProxyTime, response.getStatusCode());

            // 3. 응답 크기 로깅
            if (response.getBody() != null) {
                log.debug("AI 서버 응답 데이터: requestId={}, responseSize={}bytes",
                        requestId, response.getBody().toString().length());
            }

        } catch (Exception ex){
            long totalErrorTime = System.currentTimeMillis() - proxyStartTime;

            log.error("프록시 처리 실패: requestId={}, totalErrorTime={}ms, error={}",
                    requestId, totalErrorTime, ex.getMessage(), ex);
        }
    }

    private HttpHeaders createHttpHeaders(String requestId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Request-Id", requestId);
        headers.set("X-Frame-Format", "jpeg");
        headers.set("X-Processing-Mode", "async");
        headers.set("X-Proxy-Timestamp", String.valueOf(System.currentTimeMillis()));
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
