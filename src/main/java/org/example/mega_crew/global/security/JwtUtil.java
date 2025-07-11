package org.example.mega_crew.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final Key key;
    private final long tokenValidityMilliseconds;


    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long tokenValidityMilliseconds){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityMilliseconds = tokenValidityMilliseconds;
    }

    // token 생성
    public String createToken(String email){
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Token expiration 여부 확인
    public boolean isTokenExpired(String token){
        try{
            return parseClaims(token).getExpiration().before(new Date());
        } catch(JwtException | IllegalArgumentException e){
            return true;
        }
    }

    // Token에서 이메일 추출
    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }

    // Token 유효성 검증
    public boolean validateToken(String token){
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // HTTP 요청에서 토큰 추출
    public String extractTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    // token parsing
    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JET 토큰에서 사용자명 추출
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
