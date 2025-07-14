package org.example.mega_crew.domain.media.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TextTo3DRequest {

    @NotBlank(message = "수어로 번역할 텍스트를 입력해주세요.")
    @Size(max = 300, message = "텍스트는 300자를 초과할 수 없습니다.")
    private String text;

    public TextTo3DRequest(String text) {
        this.text = text;
    }

}
