package org.example.mega_crew.domain.media.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebcamAnalysisResponse {

    // 번역 결과 text
    private String analysisResult;

    private String message;

    public static WebcamAnalysisResponse success(String analysisResult) {
        return WebcamAnalysisResponse.builder()
                .analysisResult(analysisResult)
                .message("Success")
                .build();
    }

    public static WebcamAnalysisResponse error(String message) {
        return WebcamAnalysisResponse.builder()
                .message(message)
                .build();
    }
}
