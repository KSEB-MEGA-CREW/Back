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

    // 세션 처리 단순화 -> session 생성용 엔드포인트 제거
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
            // => DB 조회 없이 토큰에서 직접 추출 => 최적화
            // var userResponse = userService.getUserInfo(email);
            Long userId = jwtUtil.extractUserId(token); // 토큰에서 직접 추출
            if(userId==null){
                throw new IllegalArgumentException("토큰에서 사용자 ID를 찾을 수 없습니다.");
            }
            log.debug("JWT에서 사용자 ID 추출 완료: email={}, userId={}", email, userId);

            return userId;
        } catch(Exception e){
            log.error("JWT 토큰 처리 오류: {}",e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
        }
    }
}
