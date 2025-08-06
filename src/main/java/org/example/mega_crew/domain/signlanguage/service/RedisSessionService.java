package org.example.mega_crew.domain.signlanguage.service;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final long SESSION_TIMEOUT_MINUTES = 30; // 세션 만료 시간 30분으로 변경 (일반적 관행)

    // 세션 존재 여부 확인
    public boolean sessionExists(String sessionId){
        String key = SESSION_KEY_PREFIX + sessionId;
        return redisTemplate.hasKey(key);
    }

    // public createSession 메서드 추가
    public void createSession(String sessionId, Long userId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        long currentTime = System.currentTimeMillis();

        // 세션 데이터 구조를 Map으로 정의
        Map<String, String> sessionData = Map.of(
                "userId", String.valueOf(userId),
                "startTime", String.valueOf(currentTime),
                "lastActivity", String.valueOf(currentTime),
                "submissionCount", "0"
        );

        // Redis의 Hash 자료구조를 사용하여 세션 데이터를 저장
        redisTemplate.opsForHash().putAll(key, sessionData);
        // 세션 만료 시간 설정
        redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        log.info("Redis에 새 세션 생성: sessionId={}, userId={}", sessionId, userId);
    }

    /**
     * 특정 세션에 대한 소유자 ID를 확인합니다.
     */
    public boolean isSessionOwner(String sessionId, Long userId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object storedUserId = redisTemplate.opsForHash().get(key, "userId");

        if (storedUserId == null) {
            // 자동 세션 생성
            createSession(sessionId, userId);
            return true;
        }

        // TTL 갱신
        redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        return userId.equals(Long.valueOf(storedUserId.toString()));
    }

    public void recordFrameSubmission(String sessionId, int frameIndex) {
        String key = SESSION_KEY_PREFIX + sessionId;
        log.debug("세션 만료 시간 갱신: sessionId={}, frameIndex={}", sessionId, frameIndex);

        // Redis에 세션 키가 존재하면 만료 시간을 갱신
        if (redisTemplate.hasKey(key)) {
            redisTemplate.expire(key, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } else {
            // 세션이 Redis에 존재하지 않으면 새로운 세션으로 간주하고 생성하는 로직 필요
            // 현재 코드에서는 세션 생성 로직이 없으므로, 필요에 따라 추가해야 함
            log.warn("존재하지 않는 세션에 대한 만료 시간 갱신 요청: sessionId={}", sessionId);
        }
    }
}
