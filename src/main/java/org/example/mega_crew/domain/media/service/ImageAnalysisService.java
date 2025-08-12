package org.example.mega_crew.domain.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.history.Service.TranslationHistoryService;
import org.example.mega_crew.domain.history.dto.request.WebcamAnalysisHistoryRequestDto;
import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.media.dto.request.WebcamAnalysisRequest;
import org.example.mega_crew.domain.media.dto.response.WebcamAnalysisResponse;
import org.example.mega_crew.global.client.ai.AiServerClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageAnalysisService {

    private final AiServerClient aiServerClient;
    private final TranslationHistoryService historyService;

    // 히스토리 기록 포함 메서드
    public WebcamAnalysisResponse analyzeImage(WebcamAnalysisRequest request, Long userId, String userAgent) {

        WebcamAnalysisHistoryRequestDto historyRequest = WebcamAnalysisHistoryRequestDto.builder()
            .userId(userId)
            .fileName(request.getImageFile().getOriginalFilename())
            .userAgent(userAgent)
            .build();

        TranslationHistory history = historyService.startImageToTextWork(historyRequest);
        long startTime = System.currentTimeMillis();

        try {
            log.info("AI server image analysis request: {}", request.getImageFile().getOriginalFilename());

            WebcamAnalysisResponse aiResponse = aiServerClient.analysisImage(request.getImageFile(), request);
            int processingTime = (int) (System.currentTimeMillis() - startTime);

            if (aiResponse != null && aiResponse.getAnalysisResult() != null) {
                historyService.updateResult(history.getId(), aiResponse.getAnalysisResult(),
                    "SUCCESS", processingTime, null);
                return WebcamAnalysisResponse.success(aiResponse.getAnalysisResult());
            } else {
                historyService.updateResult(history.getId(), null, "ERROR", processingTime,
                    "AI server returned empty result");
                return WebcamAnalysisResponse.error("AI server image analysis response failed");
            }

        } catch (Exception e) {
            int processingTime = (int) (System.currentTimeMillis() - startTime);
            log.error("Image analysis failed: {}", e.getMessage());

            historyService.updateResult(history.getId(), null, "ERROR", processingTime, e.getMessage());
            return WebcamAnalysisResponse.error("AI server image analysis failed");
        }
    }

    // 기존 메서드 (하위 호환성 유지)
    public WebcamAnalysisResponse analyzeImage(WebcamAnalysisRequest request) {
        try {
            WebcamAnalysisResponse aiResponse = aiServerClient.analysisImage(request.getImageFile(), request);

            if (aiResponse != null) {
                return WebcamAnalysisResponse.success(aiResponse.getAnalysisResult());
            } else {
                return WebcamAnalysisResponse.error("AI server image analysis response failed");
            }
        } catch (Exception e) {
            log.error("Image analysis failed: {}", e.getMessage());
            return WebcamAnalysisResponse.error("AI server image analysis failed");
        }
    }
}
