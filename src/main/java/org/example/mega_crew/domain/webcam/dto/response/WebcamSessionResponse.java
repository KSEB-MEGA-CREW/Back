package org.example.mega_crew.domain.webcam.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebcamSessionResponse {
    private String sessionId;
    private String status;
    private String message;
    private String videoUrl;
}
