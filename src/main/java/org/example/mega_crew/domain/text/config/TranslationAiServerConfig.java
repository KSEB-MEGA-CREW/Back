package org.example.mega_crew.domain.text.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.server.translation")
@Data
public class TranslationAiServerConfig {

    private String baseUrl;
    private String endpoint = "/translate/text-to-sign";
    private int timeout = 10000; // 10s
    private int maxRetries = 1; // 재시도 최소화
}
