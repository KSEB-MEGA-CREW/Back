package org.example.mega_crew.domain.text.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguage.config.AIServerConfig;
import org.example.mega_crew.domain.text.dto.TextTranslationRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTranslationService {
    private final RestTemplate restTemplate;
    private final AIServerConfig aiServerConfig;
    private final TextTranslationService textTranslationService;

    @Async
    public void submitTranslationAsync(TextTranslationRequest request, String reuqest){
        long startTime = System.currentTimeMillis();

        try{
            String url = aiServerConfig.getBaseUrl() + aiServerConfig.gegTextToSignEndpoint();

        }
    }
}
