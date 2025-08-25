package org.example.mega_crew.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 인증 실패: {}", exception.getMessage(), exception);

        String errorMessage = getErrorMessage(exception);
        String redirectUrl = String.format("%s/auth/callback?error=%s",
                frontendUrl, URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));

        log.info("OAuth2 실패 후 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String getErrorMessage(AuthenticationException exception) {
        String exceptionName = exception.getClass().getSimpleName();

        return switch (exceptionName) {
            case "OAuth2AuthenticationException" -> "OAuth2_인증_실패";
            case "InsufficientAuthenticationException" -> "인증_정보_부족";
            case "InvalidTokenException" -> "유효하지_않은_토큰";
            case "TokenExpiredException" -> "토큰_만료";
            default -> {
                log.warn("처리되지 않은 인증 예외: {}", exceptionName);
                yield "인증_실패";
            }
        };
    }
}