package org.example.mega_crew.domain.signlanguage.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    /**
     * 새로운 세션을 Redis에 생성하고 사용자 ID와 연결합니다.
     * 이 메서드는 백엔드 컨트롤러의 세션 생성 API에서 호출됩니다.
     */
    public void createSession(String sessionId, Long userId) {
        String key = SESSION_KEY_PREFIX + sessionId;

        // 세션 데이터 구조를 Map으로 정의
        Map<String, String> sessionData = Map.of(
                "userId", String.valueOf(userId),
                "startTime", String.valueOf(System.currentTimeMillis()),
                "lastActivity", String.valueOf(System.currentTimeMillis())
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
        // Redis에 저장된 userId가 요청의 userId와 일치하는지 확인
        Object sessionOwnerId = redisTemplate.opsForHash().get(key, "userId");
        return sessionOwnerId != null && sessionOwnerId.equals(String.valueOf(userId));
    }

    /**
     * 프레임 제출 시 세션 만료 시간을 갱신합니다.
     * frameIndex는 저장하지 않고, 세션만 활성 상태로 유지합니다.
     */
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
