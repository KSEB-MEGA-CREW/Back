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


    // 히스토리 기록이 포함된 새로운 메서드
    public TextTo3DResponse generate3D(TextTo3DRequest request, Long userId, String userAgent, String clientIp) {

        // 1. 히스토리 기록 시작
        TextTo3DHistoryRequestDto historyRequest = new TextTo3DHistoryRequestDto();
        historyRequest.setUserId(userId);
        historyRequest.setTextContent(request.getText());
        historyRequest.setUserAgent(userAgent);
        historyRequest.setClientIp(clientIp);

        TranslationHistory history = historyService.startTextTo3DWork(historyRequest);
        long startTime = System.currentTimeMillis();

        try {
            log.info("AI server generate 3D request for user: {}", userId);

            // 2. 기존 AI 서버 호출 로직
            TextTo3DResponse aiResponse = aiServerClient.generate3DFromText(request);

            int processingTime = (int)(System.currentTimeMillis() - startTime);

            if (aiResponse != null && aiResponse.getAiResult() != null) {
                log.info("AI server generate 3D response success");

                // 3. 성공 기록 업데이트
                historyService.updateResult(
                    history.getId(),
                    aiResponse.getAiResult(),
                    "SUCCESS",
                    processingTime,
                    null
                );

                return TextTo3DResponse.success(aiResponse.getAiResult());
            } else {
                log.error("AI server generate 3D response failed - empty result");

                // 4. 실패 기록 업데이트
                historyService.updateResult(
                    history.getId(),
                    null,
                    "ERROR",
                    processingTime,
                    "AI server returned empty result"
                );

                return TextTo3DResponse.failure("AI server generate 3D response failed");
            }

        } catch (Exception e) {
            int processingTime = (int)(System.currentTimeMillis() - startTime);
            log.error("Text to 3D generation failed: {}", e.getMessage(), e);

            // 5. 예외 발생 시 에러 기록
            historyService.updateResult(
                history.getId(),
                null,
                "ERROR",
                processingTime,
                e.getMessage()
            );

            return TextTo3DResponse.failure("AI server generate 3D response failed");
        }
    }


    public TextTo3DResponse generate3D(TextTo3DRequest request){
        try{
            log.info("AI server generate 3D response");
            TextTo3DResponse aiResponse = aiServerClient.generate3DFromText(request);

            if(aiResponse!= null){
                log.info("AI server generate 3D response success");
                return TextTo3DResponse.success(aiResponse.getAiResult());
            }
            else{
                log.error("AI server generate 3D response failed");
                return TextTo3DResponse.failure("AI server generate 3D response failed");
            }
        } catch(Exception e){
            log.error(e.getMessage());
            return TextTo3DResponse.failure("AI server generate 3D response failed");
        }
    }
}
