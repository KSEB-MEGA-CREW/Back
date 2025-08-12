package org.example.mega_crew.domain.text.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextTranslationRequest {
    @NotBlank(message = "번역할 텍스트는 필수입니다.")
    @Size(max = 200 , message = "텍스트는 200자 이하여야 합니다.")
    private String text;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId; // JWT에서 추출해서 저장

    @NotBlank(message = "세션 ID는 필수입니다.")
    private String sessionId;

    private String language; // 현재 한국어 입력 처리만 가능 : "ko"

    @NotNull(message = "타임스탬프는 필수입니다.")
    private Long timestamp;
}
