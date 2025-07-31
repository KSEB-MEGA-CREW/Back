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
            String email = oAuth2User.getAttribute("email");
            String username = oAuth2User.getAttribute("name");

            Object providerIdObj = oAuth2User.getAttribute("id");
            String providerId = String.valueOf(providerIdObj); // String으로 변환

            // kakao의 경우 email 추출
            if(email==null){
                Map<String,Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
                if(kakaoAccount !=null){
                    email = (String) kakaoAccount.get("email");
                }
                else{ // kakao의 경우가 아님에도 불구하고 email이 존재하지 않는 경우
                    log.error("OAuth2 email이 존재하지 않습니다.");
                    response.sendRedirect(frontendUrl + "/auth/callback?error=no_email");
                    return;
                }
            }

            // kakao의 경우 username 추출
            if(username==null){
                Map<String,Object> properties = oAuth2User.getAttribute("properties");
                if(properties!=null){
                    username = (String) properties.get("nickname");
                }
            }

            String token = userService.processOAuth2Login(email, username, providerId, AuthProvider.KAKAO);

            String redirectUrl = String.format("%s/auth/callback?token=%s", frontendUrl, token);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("OAuth2 로그인 실패", e);
            response.sendRedirect(frontendUrl + "/auth/callback?error=server_error");
        }
    }

}
