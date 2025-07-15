package org.example.mega_crew.domain.media.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TextTo3DResponse {

    // aiResult -> 3D animation
    // 현재 output 타입이 미정이기에 임시로 String 사용
    private String aiResult;
    private String message;

    public static TextTo3DResponse success(String aiResult){
        return  TextTo3DResponse.builder()
                .aiResult(aiResult)
                .message("success")
                .build();
    }

    public static TextTo3DResponse failure(String message){
        return TextTo3DResponse.builder()
                .message(message)
                .build();
    }
}
