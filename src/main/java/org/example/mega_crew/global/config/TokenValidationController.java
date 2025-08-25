package org.example.mega_crew.global.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://ai-server:8000", "ws://ai-server:8000"})
public class TokenValidationController {

    private final JwtUtil jwtUtil;

    /**
     * AI 서버에서 요청하는 JWT 토큰 검증 API
     * WebSocket 연결 시 AI 서버가 호출
     */
    @PostMapping("/verify-token")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> verifyToken(
            @RequestBody TokenValidationRequest request) {

        try {
            String token = request.getToken();

            if (token == null || token.trim().isEmpty()) {
                log.warn("토큰이 비어있음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("토큰이 필요합니다"));
            }

            // JWT 토큰 유효성 검증
            if (!jwtUtil.validateToken(token)) {
                log.warn("유효하지 않은 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                return ResponseEntity.ok(
                        ApiResponse.success(TokenValidationResponse.invalid())
                );
            }

            // 사용자 ID와 이메일 추출
            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);

            if (userId == null || email == null) {
                log.warn("토큰에서 사용자 정보 추출 실패");
                return ResponseEntity.ok(
                        ApiResponse.success(TokenValidationResponse.invalid())
                );
            }

            log.info("토큰 검증 성공: userId={}, email={}", userId, email);

            return ResponseEntity.ok(
                    ApiResponse.success(TokenValidationResponse.valid(userId, email))
            );

        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return ResponseEntity.ok(
                    ApiResponse.success(TokenValidationResponse.invalid())
            );
        }
    }

    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Backend server is healthy"));
    }

    // DTO 클래스들
    public static class TokenValidationRequest {
        private String token;

        public TokenValidationRequest() {}

        public TokenValidationRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private Long userId;
        private String email;
        private String message;

        public TokenValidationResponse() {}

        public TokenValidationResponse(boolean valid, Long userId, String email, String message) {
            this.valid = valid;
            this.userId = userId;
            this.email = email;
            this.message = message;
        }

        public static TokenValidationResponse valid(Long userId, String email) {
            return new TokenValidationResponse(true, userId, email, "Token is valid");
        }

        public static TokenValidationResponse invalid() {
            return new TokenValidationResponse(false, null, null, "Token is invalid");
        }

        // Getters and Setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}