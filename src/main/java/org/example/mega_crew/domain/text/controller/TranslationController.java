package org.example.mega_crew.domain.text.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.text.dto.TextTranslationRequest;
import org.example.mega_crew.domain.text.dto.TranslationSubmissionResponse;
import org.example.mega_crew.domain.text.service.AsyncTranslationProxyService;
import org.example.mega_crew.domain.text.service.TranslationSessionService;
import org.example.mega_crew.domain.text.service.TranslationValidationService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
@Slf4j
public class TranslationController {

    private final TranslationValidationService translationValidationService;
    private final TranslationSessionService translationSessionService;
    private final AsyncTranslationProxyService asyncTranslationProxyService;
    private final JwtUtil jwtUtil;

    @PostMapping("/text-to-sign")
    public ResponseEntity<ApiResponse<TranslationSubmissionResponse>> submitText(
            @Valid @RequestBody TextTranslationRequest request,
            HttpServletRequest httpRequest) {
        String requestId = UUID.randomUUID().toString();
        long controllerStartTime = System.currentTimeMillis();
        long submissionTime =  System.currentTimeMillis();

        try{
            // 1. JWT 검증
            String token = jwtUtil.extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.extractUserId(token);

            // 2. 사용자 ID 일치 확인
            if(!userId.equals(request.getUserId())){
                log.warn("사용자 ID 불일치: JWT={}, Request={}", userId, request.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("권한이 없습니다."));
            }

            // 3. 세션 권한 확인
            if(!translationSessionService.isSessionOwner(request.getSessionId(), userId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("세션 접근 권한이 없습니다."));
            }

            // 4. 텍스트 검증
            translationValidationService.validateTranslationRequest(request);

            // 5. AI 서버로 비동기 전송 (응답 대기 안함!!!)
            asyncTranslationProxyService.submitTranslationAsync(request,requestId);

            // 6. 세션 기록
            translationSessionService.recordTranslationSubmission(request.getSessionId(), request.getText());

            long controllerEndTime = System.currentTimeMillis();
            long totalControllerTime = controllerEndTime - controllerStartTime;

            // 7. 즉시 응답 반환 => 실제 AI 처리는 웹소켓을 통해 frontend로 전송
            TranslationSubmissionResponse response = TranslationSubmissionResponse.builder()
                    .status("SUBMITTED")
                    .requestId(requestId)
                    .submissionTime(submissionTime)
                    .build();

            log.info("텍스트 번역 요청 처리 완료: requestId={}, totalTime={}ms",
                    requestId, totalControllerTime);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (UsernameNotFoundException e) {
            return handleError(requestId, submissionTime, "사용자를 찾을 수 없습니다.",
                    HttpStatus.UNAUTHORIZED, controllerStartTime);

        } catch (IllegalArgumentException e) {
            log.warn("텍스트 번역 검증 실패: requestId={}, error={}", requestId, e.getMessage());
            return handleError(requestId, submissionTime, e.getMessage(),
                    HttpStatus.BAD_REQUEST, controllerStartTime);
        } catch (Exception e) {
            log.error("텍스트 번역 요청 처리 오류: requestId={}", requestId, e);
            return handleError(requestId,submissionTime,"서버 내부 오류가 발생하였습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR, controllerStartTime);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("번역 서비스 정상 동작"));
    }

    private ResponseEntity<ApiResponse<TranslationSubmissionResponse>> handleError(
            String requestId, Long submissionTime, String message,
            HttpStatus status, long startTime) {

        long errorTime = System.currentTimeMillis() - startTime;
        log.error("요청 처리 실패: requestId={}, errorTime={}ms, message={}",
                requestId, errorTime, message);

        TranslationSubmissionResponse response = TranslationSubmissionResponse.builder()
                .status("FAILED")
                .requestId(requestId)
                .submissionTime(submissionTime)
                .build();

        return ResponseEntity.status(status).body(ApiResponse.error(response.toString()));
    }
}
