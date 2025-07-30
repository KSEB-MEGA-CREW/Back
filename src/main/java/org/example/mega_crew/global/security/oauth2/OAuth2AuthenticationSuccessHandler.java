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
import java.util.Map;

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
            String registrationId = null;
            if (authentication instanceof OAuth2AuthenticationToken) {
                registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            }

            String email = null;
            String username = null;
            String providerId = null;

            // 소셜 종류별로 파싱 로직 분기
            switch (registrationId) {
                case "google":
                    providerId = oAuth2User.getAttribute("sub");
                    email = oAuth2User.getAttribute("email");
                    username = oAuth2User.getAttribute("name");
                    break;
                case "naver":
                    providerId = oAuth2User.getAttribute("id");
                    email = oAuth2User.getAttribute("email");
                    username = oAuth2User.getAttribute("name");
                    break;
                case "kakao":
                    providerId = String.valueOf(oAuth2User.getAttribute("id"));
                    Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
                    if (kakaoAccount != null) {
                        email = (String) kakaoAccount.get("email");
                        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                        if (profile != null) {
                            username = (String) profile.get("nickname");
                        }
                    }
                    break;
                default:
                    providerId = oAuth2User.getName();
            }


            // 꼭 로그로 확인! 실제 값이 잘 파싱됐는지
            log.info("Parsed info - registrationId: {}, providerId: {}, email: {}, username: {}", registrationId, providerId, email, username);

            if(email == null) {
                log.error("OAuth2 email이 존재하지 않습니다.");
                response.sendRedirect(frontendUrl + "/auth/callback?error=no_email");
                return;
            }

            AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
            String token = userService.processOAuth2Login(email, username, providerId, provider);

            String redirectUrl = String.format("%s/auth/callback?token=%s", frontendUrl, token);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 로그인 실패", e);
            response.sendRedirect(frontendUrl + "/auth/callback?error=server_error");
        }
    }

}
