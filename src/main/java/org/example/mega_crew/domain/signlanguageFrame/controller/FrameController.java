package org.example.mega_crew.domain.signlanguageFrame.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguageFrame.dto.FrameRequest;
import org.example.mega_crew.domain.signlanguageFrame.dto.FrameSubmissionResponse;
import org.example.mega_crew.domain.signlanguageFrame.service.AsyncAIProxyService;
import org.example.mega_crew.domain.signlanguageFrame.service.FrameValidationService;
import org.example.mega_crew.domain.signlanguageFrame.service.RedisSessionService;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<FrameSubmissionResponse>> submitFrame(
            @Valid @RequestBody FrameRequest request,
            HttpServletRequest httpRequest) {

        String requestId = UUID.randomUUID().toString();
        long submissionTime = System.currentTimeMillis();

        try{
            // 1. JWT -> 사용자 ID 추출
            Long userId = extractUserIdFromRequest(httpRequest);

            // 2. 요청 사용자 ID 검증
            if(!userId.equals(request.getUserId())){
                log.warn("사용자 ID 불일치: JWT={}, Request={}",userId,request.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("권한이 없습니다.")); // 사용자 ID 검증 오류 -> 권한 없음 처리
            }

            // 3. 세션 소유권 검증
            if(!redisSessionService.isSessionOwner(request.getSessionId(), userId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("세션 접근 권한이 없습니다.")); // 소유권 검증 오류
            }

            // 4. 프레임 데이터 검증
            frameValidationService.validateFrame(request);

            // 5. AI 서버로 비동기 전송
            asyncAIProxyService.submitFrameAsync(request,requestId);

            // 6. 세션 제출 기록
            redisSessionService.recordFrameSubmission(request.getSessionId(), request.getFrameIndex());

            // 7. 간단한 성공 응답
            FrameSubmissionResponse response = FrameSubmissionResponse.builder()
                    .status("SUBMITTED")
                    .requestId(requestId)
                    .submissionTime(submissionTime)
                    .build();

            log.info("프레임 제출 완료: requestId={}, sessionId={}, frameIndex={}, userId={}",
                    requestId, request.getSessionId(), request.getFrameIndex(), userId);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (UsernameNotFoundException e) {
            log.warn("사용자를 찾을 수 없음: requestId={}, error={}", requestId,e.getMessage());

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
