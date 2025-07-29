package org.example.mega_crew.domain.signlanguage.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync // 비동기 처리를 위한 annotation
@ConfigurationProperties(prefix = "ai.server")
@Data
public class AIServerConfig {
    private String url = "http://localhost:5000"; // 임시 AI Server 주소
    private String endpoint = "/analyze-frame";
    private int timeout = 5000;
    private int maxRetries = 3; // 최대 재시도 횟수

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
