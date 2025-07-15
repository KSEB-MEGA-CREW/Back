package org.example.mega_crew.global.security;

import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.user.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (API 서버이므로)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 설정 (필요에 따라 조정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger 관련 경로 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api-docs/**"  // 추가
                        ).permitAll()

                        // 기존 허용 경로
                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()

                        // WebSocket 경로 허용 (필요시)
                        .requestMatchers("/video-stream/**", "/websocket/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // 정적 리소스 허용
                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .defaultSuccessUrl("/api/auth/oauth2/success")
                        .failureUrl("/api/auth/oauth2/failure")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",    // React 개발 서버
                "http://localhost:8080",    // Spring Boot 서버 (Swagger UI)
                "http://127.0.0.1:8080",    // 로컬 IP
                "http://127.0.0.1:3000"     // 로컬 IP React
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}