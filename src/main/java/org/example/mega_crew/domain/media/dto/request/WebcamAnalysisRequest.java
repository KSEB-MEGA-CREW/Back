package org.example.mega_crew.domain.media.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class WebcamAnalysisRequest {

    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile imageFile;

    public WebcamAnalysisRequest(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}
