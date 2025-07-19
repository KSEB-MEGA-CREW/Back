package org.example.mega_crew.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try{
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String username = oAuth2User.getAttribute("username");
            String providerId = oAuth2User.getAttribute("providerId");

            if(email == null){ // OAuth2 사용자 이메일 정보가 없을 때 처리 -> FailureHandler 사용하지 않고 SuccessHandler의 예외로 처리 => 무엇이 더 나을지 고민하기
                log.error("OAuth2 email dose not exist.");
                response.sendRedirect("http://localhost:3000/auth/callback?error=no_email");

                return;
            }
            // UserService를 통한 OAuth2 login 처리
            String token = userService.processOAuth2Login(email,username, providerId, AuthProvider.GOOGLE);

            String redirectUrl = String.format("http://localhost:3000/auth/callback?token=%s", token);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 login failed.", e);
            response.sendRedirect("http://localhost:3000/auth/callback?error=server_error");
        }
    }
}
