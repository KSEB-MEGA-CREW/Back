package org.example.mega_crew.domain.text.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String text;

    @NotNull
    private Long userId; // JWT에서 추출해서 저장


}
