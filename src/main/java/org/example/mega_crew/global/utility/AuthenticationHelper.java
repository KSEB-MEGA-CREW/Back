package org.example.mega_crew.global.utility;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

   private final JwtUtil jwtUtil;
   private final UserService userService;

   // http 요청에서 사용자 id 추출
   public Long extractUserIdFromRequest(HttpServletRequest request){
      try {
         // 1. JWT 토큰 추출
         String token = jwtUtil.extractTokenFromRequest(request);
         validateToken(token);

         // 2. 토큰에서 이메일 추출
         String email = jwtUtil.extractEmail(token);
         validateEmail(email);

         // 3. 3. UserService를 통해 이메일로 사용자 정보 조회
         var userResponse = userService.getUserInfo(email);

         log.debug("JWT에서 사용자 ID 추출 완료: email={}, userId={}", email, userResponse.getId());
         return userResponse.getId();

      } catch (UsernameNotFoundException e) {
         log.error("사용자를 찾을 수 없음: {}", e.getMessage());
         throw e;
      } catch (Exception e) {
         log.error("JWT 토큰 처리 오류: {}", e.getMessage());
         throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
      }
   }


   // 사용자 권한 검증
   public void validateUserAccess(HttpServletRequest request, Long requestedUserId) {
      Long tokenUserId = extractUserIdFromRequest(request);
      if (!tokenUserId.equals(requestedUserId)) {
         throw new SecurityException("권한이 없습니다.");
      }
   }

   private void validateToken(String token) {
      if (token == null || token.trim().isEmpty()) {
         throw new IllegalArgumentException("JWT 토큰이 없습니다.");
      }
   }

   private void validateEmail(String email) {
      if (email == null || email.trim().isEmpty()) {
         throw new IllegalArgumentException("JWT 토큰에서 이메일을 추출할 수 없습니다.");
      }
   }
}
