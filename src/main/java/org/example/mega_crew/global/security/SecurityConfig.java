package org.example.mega_crew.global.security;

import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.user.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 / CSRF 비활성화 / 세션 관리 설정 / 인증,인가 설정 / OAuth2 로그인 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api-docs/**"
                        ).permitAll()

                                // 인증 관련 경로
                                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()


                        // WebSocket 경로 허용 (필요시)
                        .requestMatchers("/video-stream/**", "/websocket/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                                // WebSocket 및 웹캠 경로 (개발 단계에서는 permitAll, 추후 authenticated로 변경)
                                .requestMatchers("/ws-webcam/**", "/websocket/**").permitAll()
                                .requestMatchers("/api/webcam/**").permitAll() // 추후 .authenticated()로 변경
                                .requestMatchers("/api/webcam/**").permitAll() // webcam test용
                                // 정적 리소스
                                .requestMatchers("/", "/error", "/favicon.ico", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**").permitAll()


                                // 나머지는 인증 필요
                                .anyRequest().authenticated()
                        )
                        .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(oAuth2UserService))
                                .defaultSuccessUrl("/api/auth/oauth2/success", true)
                                .failureUrl("/api/auth/oauth2/failure")
//                                .successHandler(oAuth2AuthenticationSuccessHandler())  추후 OAuth2 성공/실패 핸들러 구현한 다음,
//                                .failureHandler(oAuth2AuthenticationFailureHandler())  위의 두 메서드 대신 JWT 토큰 생성 핸들러 추가하기
                        );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",    // React 개발 서버
                "http://localhost:8080",    // Spring Boot 서버
                "http://127.0.0.1:8080",    // 로컬 IP
                "http://127.0.0.1:3000"   // 로컬 IP React
//                "https://yourdomain.com"    추후 프로덕션 도메인 생성 후 추가하기
        ));

        // 헤더 전체 허용
        corsConfiguration.addAllowedHeader("*");

        // 개발 단계이므로 편의상 모든 메소드 허용 추후 수정하기
        corsConfiguration.addAllowedMethod("*");

        // 자격 증명 허용
        corsConfiguration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 default
        corsConfiguration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
}