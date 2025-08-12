package org.example.mega_crew.domain.text.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TRANSLATION_SESSION_PREFIX = "translation_session:";
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    public boolean isSessionOwner(String sessionId, Long userId){
        String key = TRANSLATION_SESSION_PREFIX + sessionId;
        Object storedUserId = redisTemplate.opsForHash().get(key, "userId");

        if(storedUserId == null){
            // 자동 세션 생성
            createTranslationSession(sessionId, userId);
            return true;
        }

        // TTL 갱신
        redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        return userId.equals(Long.valueOf(storedUserId.toString()));
    }

    public void createTranslationSession(String sessionId, Long userId){
        String key = TRANSLATION_SESSION_PREFIX + sessionId;
        long currentTime = System.currentTimeMillis();

        Map<String, String> sessionData = Map.of(
                "userId", String.valueOf(userId),
                "startTime", String.valueOf(currentTime),
                "lastActivity", String.valueOf(currentTime),
                "translationCount", "0"
        );

        redisTemplate.opsForHash().putAll(key,sessionData);
        redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        log.info("번역 세션 생성: sessionId={}, userId={}", sessionId, userId);
    }

    public void recordTranslationSubmission(String sessionId, String text){
        String key = TRANSLATION_SESSION_PREFIX + sessionId;

        if(redisTemplate.hasKey(key)){
            // 번역 횟수 증가
            redisTemplate.opsForHash().increment(key, "translationCount", 1);
            // 마지막 활동시간 업데이트
            redisTemplate.opsForHash().put(key, "lastActivity", String.valueOf(System.currentTimeMillis()));
            // TTL 갱신
            redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            log.debug("번역 세션 기록 업데이트: sessionId={}, textLength={}", sessionId, text.length());
        } else {
            log.warn("존재하지 않는 번역 세션: sessionId={}", sessionId);
        }
    }
}
