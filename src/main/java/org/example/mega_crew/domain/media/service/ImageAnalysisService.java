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

    // 히스토리 기록이 포함된 새로운 메서드
    public WebcamAnalysisResponse analyzeImage(WebcamAnalysisRequest request, Long userId, String userAgent, String clientIp) {

        // 1. 히스토리 기록 시작
        WebcamAnalysisHistoryRequestDto historyRequest = new WebcamAnalysisHistoryRequestDto();
        historyRequest.setUserId(userId);
        historyRequest.setFileName(request.getImageFile().getOriginalFilename());
        historyRequest.setUserAgent(userAgent);
        historyRequest.setClientIp(clientIp);

        TranslationHistory history = historyService.startImageToTextWork(historyRequest);
        long startTime = System.currentTimeMillis();

        try {
            log.info("AI server image analysis request: {}", request.getImageFile().getOriginalFilename());

            // 2. 기존 AI 서버 호출 로직
            WebcamAnalysisResponse aiResponse = aiServerClient.analysisImage(request.getImageFile(), request);

            int processingTime = (int)(System.currentTimeMillis() - startTime);

            if (aiResponse != null && aiResponse.getAnalysisResult() != null) {
                log.info("AI server image analysis response: {}", aiResponse.getAnalysisResult());

                // 3. 성공 기록 업데이트
                historyService.updateResult(
                    history.getId(),
                    aiResponse.getAnalysisResult(),
                    "SUCCESS",
                    processingTime,
                    null
                );

                return WebcamAnalysisResponse.success(aiResponse.getAnalysisResult());
            } else {
                log.warn("AI server image analysis response failed - empty result");

                // 4. 실패 기록 업데이트
                historyService.updateResult(
                    history.getId(),
                    null,
                    "ERROR",
                    processingTime,
                    "AI server returned empty result"
                );

                return WebcamAnalysisResponse.error("AI server image analysis response failed");
            }

        } catch (Exception e) {
            int processingTime = (int)(System.currentTimeMillis() - startTime);
            log.error("Image analysis failed: {}", e.getMessage(), e);

            // 5. 예외 발생 시 에러 기록
            historyService.updateResult(
                history.getId(),
                null,
                "ERROR",
                processingTime,
                e.getMessage()
            );

            return WebcamAnalysisResponse.error("AI server image analysis failed");
        }
    }


    public WebcamAnalysisResponse analyzeImage(WebcamAnalysisRequest request) {
        try{
            log.info("AI server generate 3D request: {}", request.getImageFile().getOriginalFilename());

            WebcamAnalysisResponse aiResponse = aiServerClient.analysisImage(request.getImageFile(), request);

            if(aiResponse!=null){
                log.info("AI server generate 3D response: {}", aiResponse.getAnalysisResult());
                return WebcamAnalysisResponse.success(aiResponse.getAnalysisResult());
            }
            else{
                log.info("AI server generate 3D response failed");
                return WebcamAnalysisResponse.error("AI server generate 3D response failed");
            }
        } catch(Exception e){
            log.error(e.getMessage());
            return WebcamAnalysisResponse.error("AI server generate 3D response failed");
        }
    }

}
