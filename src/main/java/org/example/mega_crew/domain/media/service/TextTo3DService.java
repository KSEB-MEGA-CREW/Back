package org.example.mega_crew.domain.media.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.history.Service.TranslationHistoryService;
import org.example.mega_crew.domain.history.dto.request.TextTo3DHistoryRequestDto;
import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.media.dto.request.TextTo3DRequest;
import org.example.mega_crew.domain.media.dto.response.TextTo3DResponse;
import org.example.mega_crew.global.client.ai.AiServerClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextTo3DService {

    private final AiServerClient aiServerClient;
    private final TranslationHistoryService historyService;


    // 히스토리 기록 포함 메서드
    public TextTo3DResponse generate3D(TextTo3DRequest request, Long userId, String userAgent) {

        TextTo3DHistoryRequestDto historyRequest = TextTo3DHistoryRequestDto.builder()
            .userId(userId)
            .textContent(request.getText())
            .userAgent(userAgent)
            .build();

        TranslationHistory history = historyService.startTextTo3DWork(historyRequest);
        long startTime = System.currentTimeMillis();

        try {
            log.info("AI server generate 3D request for user: {}", userId);

            TextTo3DResponse aiResponse = aiServerClient.generate3DFromText(request);
            int processingTime = (int) (System.currentTimeMillis() - startTime);

            if (aiResponse != null && aiResponse.getAiResult() != null) {
                historyService.updateResult(history.getId(), aiResponse.getAiResult(),
                    "SUCCESS", processingTime, null);
                return TextTo3DResponse.success(aiResponse.getAiResult());
            } else {
                historyService.updateResult(history.getId(), null, "ERROR", processingTime,
                    "AI server returned empty result");
                return TextTo3DResponse.failure("AI server generate 3D response failed");
            }

        } catch (Exception e) {
            int processingTime = (int) (System.currentTimeMillis() - startTime);
            log.error("Text to 3D generation failed: {}", e.getMessage());

            historyService.updateResult(history.getId(), null, "ERROR", processingTime, e.getMessage());
            return TextTo3DResponse.failure("AI server generate 3D response failed");
        }
    }

    // 기존 메서드 (하위 호환성 유지)
    public TextTo3DResponse generate3D(TextTo3DRequest request) {
        try {
            TextTo3DResponse aiResponse = aiServerClient.generate3DFromText(request);

            if (aiResponse != null) {
                return TextTo3DResponse.success(aiResponse.getAiResult());
            } else {
                return TextTo3DResponse.failure("AI server generate 3D response failed");
            }
        } catch (Exception e) {
            log.error("Text to 3D generation failed: {}", e.getMessage());
            return TextTo3DResponse.failure("AI server generate 3D response failed");
        }
    }
}
