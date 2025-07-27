package org.example.mega_crew.domain.signlanguageFrame.service;


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
    // Redis 사용
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key 접두사
    private static final String SESSION_KEY_PREFIX = "signlanguageframe:session:";
    private static final long SESSION_TIMEOUT = 30; // TTL : 30m -> 세션 캐시 time to live

    public boolean isSessionOwner(String sessionId, Long userId) {
        String key = SESSION_KEY_PREFIX + sessionId;

        // Hash에서 userId 조회
        Object storedUserId = redisTemplate.opsForHash().get(key, "userId");

        if(storedUserId==null){
            createSession(sessionId, userId);
            return true;
        }

        // TTL 갱신
        redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.MINUTES);

        return userId.equals(Long.valueOf(storedUserId.toString()));
    }

    public void recordFrameSubmission(String sessionId, Integer frameIndex) {
        String key = SESSION_KEY_PREFIX + sessionId;

        if(!redisTemplate.hasKey(key)){
            log.warn("세션을 찾을 수 없습니다: sessionId={}", sessionId);
            return;
        }

        // Hash 필드 개별 업데이트
        redisTemplate.opsForHash().put(key,"lastFrameindex",frameIndex);
        redisTemplate.opsForHash().put(key,"lastActivity",System.currentTimeMillis());
        redisTemplate.opsForHash().increment(key,"submissionCount",1);

        // TTL 갱신
        redisTemplate.expire(key,SESSION_TIMEOUT, TimeUnit.MINUTES);

        // 로깅용 현재 제출 횟수 조회
        Object submissionCount = redisTemplate.opsForHash().get(key,"submissionCount");
        log.debug("프레임 제출 기록: sessionId={}, frameIndex={}, totalSubmissions={}",
                sessionId, frameIndex, submissionCount);
    }

    public Map<Object, Object> getSessionInfo(String sessionId){
        String key = SESSION_KEY_PREFIX + sessionId;
        return redisTemplate.opsForHash().entries(key);
    }

    private void createSession(String sessionId, Long userId){
        String key = SESSION_KEY_PREFIX + sessionId;
        long currentTime = System.currentTimeMillis();

        // Hash로 세션 정보 저장
        Map<String, Object> sessionData = Map.of(
          "sessionId", sessionId,
          "userId",userId,
          "startTime",currentTime,
          "lastActivity",currentTime,
          "submissionCount",0
        );

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.MINUTES);

        log.info("새 세션 생성: sessionId={}, userId={}, TTL={}", sessionId, userId, SESSION_TIMEOUT);
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionInfo{
        private String sessionId;
        private Long userId;
        private Long startTime;
        private Long lastActivity;
        private Integer lastFrameIndex;
        private Integer submissionCount;

        public void incrementSubmissionCount(){
            if(this.submissionCount == null){
                this.submissionCount = 0; // initialize
            }
            this.submissionCount++;
        }
    }
}
