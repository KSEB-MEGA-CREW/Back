package org.example.mega_crew.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.user.entity.AuthProvider;
import org.example.mega_crew.domain.user.service.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;

    @Value("${app.frontend.url}") // application.yml의 설정 사용
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String username = oAuth2User.getAttribute("name"); // Google은 "name" 사용
            String providerId = oAuth2User.getAttribute("sub"); // Google은 "sub" 사용

            if(email == null) {
                log.error("OAuth2 email이 존재하지 않습니다.");
                response.sendRedirect(frontendUrl + "/auth/callback?error=no_email");
                return;
            }

            // 동적으로 provider 값 구하기
            String registrationId = null;
            if (authentication instanceof OAuth2AuthenticationToken) {
                registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId(); // google, naver 등
            }
            AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

            String token = userService.processOAuth2Login(email, username, providerId, provider);

            String redirectUrl = String.format("%s/auth/callback?token=%s", frontendUrl, token);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 로그인 실패", e);
            // 수정: 환경변수 사용
            response.sendRedirect(frontendUrl + "/auth/callback?error=server_error");
        }
    }
}
