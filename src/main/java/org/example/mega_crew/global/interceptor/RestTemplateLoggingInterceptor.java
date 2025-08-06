package org.example.mega_crew.global.interceptor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 성능 측정용 인터셉터 추가
@Component
@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        long startTime = System.currentTimeMillis();
        String requestId = request.getHeaders().getFirst("X-Request-Id");

        log.info("AI 서버 요청 시작: requestId={}, url={}, method={}, bodySize={}bytes",
                requestId, request.getURI(), request.getMethod(), body.length);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("AI 서버 응답 완료: requestId={}, status={}, duration={}ms",
                    requestId, response.getStatusCode(), duration);

            return response;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("AI 서버 요청 실패: requestId={}, duration={}ms, error={}",
                    requestId, duration, e.getMessage());
            throw e;
        }
    }
}
