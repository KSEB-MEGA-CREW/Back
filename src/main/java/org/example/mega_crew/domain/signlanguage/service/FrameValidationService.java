package org.example.mega_crew.domain.signlanguage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguage.dto.FrameRequest;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrameValidationService {
    // 백엔드에선 단순 검증(토큰으로 사용자 권한 검증) 수행
    // 이미지 관련 처리는 ai 서버 전담
    // => 책임 분리

    // 현재 하드코딩된 상수값들 추후 환경변수로 수정하기
    // 검증을 위한 최소한의 상수값들
    private static final long MAX_TIMESTAMP_DIFF = 5 * 60 * 1000; // 5분
    private static final int MAX_FRAME_INDEX = 864000; // 24시간 * 10fps

    public void validateFrame(FrameRequest request){
        log.debug("프레임 검증 시작: sessionId={}, frameIndex={}",
                request.getSessionId(), request.getFrameIndex());

        validateFrameData(request.getFrameData());
        validateTimestamp(request.getTimestamp());
        validateSessionId(request.getSessionId());
        validateFrameIndex(request.getFrameIndex());
        validateUserId(request.getUserId());

        log.debug("프레임 검증 완료: sessionId={}, frameIndex={}",
                request.getSessionId(), request.getFrameIndex());
    }

    private void validateFrameData(String frameData){
        if(frameData == null || frameData.trim().isEmpty()){
            throw new IllegalArgumentException("프레임 데이터가 없습니다.");
        }

        // Base64 형식만 검증 (크기 검증 제거 - AI 서버에서 처리)
        try{
            Base64.getDecoder().decode(frameData);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("잘못된 Base64 형식입니다.");
        }
    }

    private void validateTimestamp(Long timestamp){
        if(timestamp == null){
            throw new IllegalArgumentException("타임스탬프는 필수입니다.");
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);

        if(timeDiff > MAX_TIMESTAMP_DIFF){
            throw new IllegalArgumentException("타임스탬프가 유효하지 않습니다.");
        }
    }

    private void validateSessionId(String sessionId){
        if(sessionId == null || sessionId.trim().isEmpty()){
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }

        // UUID 형식 검증 (간소화)
        if(!sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")){
            throw new IllegalArgumentException("잘못된 세션 ID 형식입니다.");
        }
    }

    private void validateFrameIndex(Integer frameIndex) {
        if (frameIndex == null) {
            throw new IllegalArgumentException("프레임 인덱스는 필수입니다.");
        }

        if (frameIndex < 0) {
            throw new IllegalArgumentException("프레임 인덱스는 0 이상이어야 합니다.");
        }

        if (frameIndex > MAX_FRAME_INDEX) {
            throw new IllegalArgumentException("프레임 인덱스가 너무 큽니다. (최대: " + MAX_FRAME_INDEX + ")");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if (userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
    }
}
