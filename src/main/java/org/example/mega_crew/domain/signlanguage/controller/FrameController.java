package org.example.mega_crew.domain.signlanguage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguage.dto.FrameRequest;
import org.example.mega_crew.domain.signlanguage.dto.FrameSubmissionResponse;
import org.example.mega_crew.domain.signlanguage.service.AsyncAIProxyService;
import org.example.mega_crew.domain.signlanguage.service.FrameValidationService;
import org.example.mega_crew.domain.signlanguage.service.RedisSessionService;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/signlanguage")
@RequiredArgsConstructor
@Slf4j
public class FrameController {
    private final FrameValidationService frameValidationService;
    private final AsyncAIProxyService asyncAIProxyService;
    private final RedisSessionService redisSessionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 세션을 생성하고 세션 ID를 반환하는 API
     * 프레임 전송을 시작하기 전에 호출됩니다.
     */
    @PostMapping("/session") // 새로운 세션 생성 엔드포인트
    public ResponseEntity<ApiResponse<Map<String, String>>> createSession(HttpServletRequest httpRequest) {
        try {
            // 1. JWT에서 사용자 ID 추출 (인증된 사용자만 세션 생성 가능)
            Long userId = extractUserIdFromRequest(httpRequest);

            // 2. 새로운 세션 ID 생성
            String sessionId = UUID.randomUUID().toString();

            // 3. Redis에 세션 정보 저장 (userId와 연결)
            redisSessionService.createSession(sessionId, userId);

            log.info("새 세션 생성 완료: sessionId={}, userId={}", sessionId, userId);

            // 4. 생성된 세션 ID를 응답으로 반환
            return ResponseEntity.ok(ApiResponse.success(Map.of("sessionId", sessionId)));

        } catch (UsernameNotFoundException e) {
            log.error("세션 생성 실패: 사용자를 찾을 수 없음 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("사용자를 찾을 수 없습니다."));
        } catch (IllegalArgumentException e) {
            log.error("세션 생성 실패: JWT 토큰 문제 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("세션 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("세션 생성 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<FrameSubmissionResponse>> submitFrame(
            @Valid @RequestBody FrameRequest request,
            HttpServletRequest httpRequest) {

        String requestId = UUID.randomUUID().toString();
        long controllerStartTime = System.currentTimeMillis();
        long submissionTime = System.currentTimeMillis();

        try{
            // 프레임 크기 로깅
            int frameSize = request.getFrameData().length();
            log.info("프레임 수신: requestId={}, sessionId={}, frameIndex={}, frameSize={}bytes",
                    requestId, request.getSessionId(), request.getFrameIndex(), frameSize);

            // 1. JWT 검증 시간 측정
            long authStartTime = System.currentTimeMillis();
            Long userId = extractUserIdFromRequest(httpRequest);
            long authEndTime = System.currentTimeMillis();

            log.debug("JWT 검증 완료: requestId={}, authTime={}ms",
                    requestId, (authEndTime - authStartTime));

            // 2. 검증 시간 측정
            long validationStartTime = System.currentTimeMillis();

            if(!userId.equals(request.getUserId())){
                log.warn("사용자 ID 불일치: JWT={}, Request={}", userId, request.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("권한이 없습니다."));
            }

            if(!redisSessionService.isSessionOwner(request.getSessionId(), userId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("세션 접근 권한이 없습니다."));
            }

            frameValidationService.validateFrame(request);

            long validationEndTime = System.currentTimeMillis();
            log.debug("검증 완료: requestId={}, validationTime={}ms",
                    requestId, (validationEndTime - validationStartTime));

            // 3. 비동기 전송
            long asyncStartTime = System.currentTimeMillis();
            asyncAIProxyService.submitFrameAsync(request, requestId);
            long asyncEndTime = System.currentTimeMillis();

            log.debug("비동기 전송 시작: requestId={}, asyncSubmitTime={}ms",
                    requestId, (asyncEndTime - asyncStartTime));

            // 4. 세션 기록
            redisSessionService.recordFrameSubmission(request.getSessionId(), request.getFrameIndex());

            long controllerEndTime = System.currentTimeMillis();
            long totalControllerTime = controllerEndTime - controllerStartTime;

            // 5. 응답 생성
            FrameSubmissionResponse response = FrameSubmissionResponse.builder()
                    .status("SUBMITTED")
                    .requestId(requestId)
                    .submissionTime(submissionTime)
                    .build();

            log.info("컨트롤러 처리 완료: requestId={}, totalControllerTime={}ms, userId={}",
                    requestId, totalControllerTime, userId);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (UsernameNotFoundException e) {
            long errorTime = System.currentTimeMillis() - controllerStartTime;
            log.error("컨트롤러 처리 실패: requestId={}, errorTime={}ms, error={}",
                    requestId, errorTime, e.getMessage());

            FrameSubmissionResponse response = FrameSubmissionResponse.builder()
                    .status("USER_NOT_FOUND")
                    .requestId("사용자를 찾을 수 없습니다.")
                    .submissionTime(submissionTime)
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(response.toString()));

        } catch(IllegalArgumentException e){
            log.warn("프레임 검증 실패: requestId={}, error={}", requestId,e.getMessage());

            FrameSubmissionResponse response = FrameSubmissionResponse.builder()
                    .status("VALIDATION_ERROR")
                    .requestId(requestId)
                    .submissionTime(submissionTime)
                    .build();

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.toString()));

        } catch(Exception e){
            log.error("프레임 제출 오류: requestId={}", requestId, e);

            FrameSubmissionResponse response = FrameSubmissionResponse.builder()
                    .status("FAILED")
                    .requestId(requestId)
                    .submissionTime(submissionTime)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(response.toString()));
        }
    }

    /**
     *  AI 서버 health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck(){
        return ResponseEntity.ok(ApiResponse.success("ai 서버 정상 동작"));
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출하는 메서드
     */
    private Long extractUserIdFromRequest(HttpServletRequest request){
        try{
            // 1. Http 요청에서 JWT 토큰 추출
            String token = jwtUtil.extractTokenFromRequest(request);
            if(token == null||token.trim().isEmpty()){
                throw new IllegalArgumentException("JWT 토큰이 없습니다.");
            }

            // 2. JWT 토큰에서 이메일 추출
            String email = jwtUtil.extractEmail(token);
            if(email==null||email.trim().isEmpty()){
                throw new IllegalArgumentException("JWT 토큰에서 이메일을 추출할 수 없습니다.");
            }

            // 3. UserService를 통해 이메일로 사용자 정보 조회
            var userResponse = userService.getUserInfo(email);

            log.debug("JWT에서 사용자 ID 추출 완료: email={}, userId={}", email, userResponse.getId());

            return userResponse.getId();
        } catch(UsernameNotFoundException e){
            log.error("사용자를 찾을 수 없음: {}", e.getMessage());
            throw e; // 에러는 우선 상위로 넘김
        } catch(Exception e){
            log.error("JWT 토큰 처리 오류: {}",e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
        }
    }
}
