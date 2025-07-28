package org.example.mega_crew.domain.signlanguageFrame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.signlanguageFrame.dto.FrameRequest;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrameValidationService {
    // 최대 프레임 사이즈 제한
    // ai 서버와 프론트 엔드 상태 확인 후 조정 필요

    private static final int MAX_FRAME_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MIN_FRAME_SIZE = 1024; // 1KB
    private static final long MAX_TIMESTAMP_DIFF = 5*60*1000; // 5분

    public void validateFrame(FrameRequest request){

    }

    private void validateFrameData(String frameData){
        if(frameData == null || frameData.trim().isEmpty()){
            throw new IllegalArgumentException("프레임 데이터가 없습니다.");
        }

        try{
            byte[] decodedBytes = Base64.getDecoder().decode(frameData);
            validateFrameSize(decodedBytes.length);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("잘못된 Base64 형식입니다.");
        }
    }

    private void validateFrameSize(int size){
        if(size < MIN_FRAME_SIZE || size > MAX_FRAME_SIZE){
            throw new IllegalArgumentException("프레임 크기가 올바르지 않습니다.");
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

        if(!sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")){
            throw new IllegalArgumentException("잘못된 세션 ID 형식입니다.");
        }
    }
}
