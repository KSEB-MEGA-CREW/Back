package org.example.mega_crew.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:3000",          // 프론트엔드 개발서버
                        "http://localhost:8000",          // AI 서버 로컬
                        "http://ai-server:8000",          // AI 서버 Docker
                        "ws://localhost:8000",            // WebSocket 로컬
                        "ws://ai-server:8000",            // WebSocket Docker
                        "https://your-frontend-domain.com", // 프로덕션 프론트엔드
                        "https://your-ai-domain.com"      // 프로덕션 AI서버
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1시간 캐시
    }
}