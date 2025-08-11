package org.example.mega_crew.domain.signlanguage.config;


import lombok.Data;
import lombok.Getter;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.example.mega_crew.global.interceptor.RestTemplateLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync // 비동기 처리를 위한 annotation
@ConfigurationProperties(prefix = "ai.server")
@Data
@Getter
public class AIServerConfig { // Apache HttpClient 5에서 변경된 API에 맞춰 메서드 수정
    private String baseUrl; // Value annotation 제거
    private String endpoint = "/analyze-frame";
    private int timeout = 5000;
    private int maxRetries = 3; // 최대 재시도 횟수
    private int maxPools = 100;

    @Bean
    public RestTemplate restTemplate() {
        // Connection Manager 설정
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxPools);
        connectionManager.setDefaultMaxPerRoute(20);

        // Request Config 설정 (HttpClient 5 방식)
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .build();

        // HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // HttpComponentsClientHttpRequestFactory 설정
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);

        // 로깅 인터셉터 추가
        restTemplate.getInterceptors().add(new RestTemplateLoggingInterceptor());

        return restTemplate;
    }
}
