package org.example.mega_crew.domain.signlanguage.config;


import lombok.Data;
import org.example.mega_crew.global.interceptor.RestTemplateLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
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
public class AIServerConfig {
    @Value("${ai.server.base-url}")
    private String url;
    private String endpoint = "/analyze-frame";
    private int timeout = 5000;
    private int maxRetries = 3; // 최대 재시도 횟수

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        // 로깅 인터셉터 추가
        restTemplate.getInterceptors().add(new RestTemplateLoggingInterceptor());

        return restTemplate;
    }
}
