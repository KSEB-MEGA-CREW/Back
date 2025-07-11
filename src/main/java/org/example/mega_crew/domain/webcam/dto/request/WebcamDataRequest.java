package org.example.mega_crew.domain.webcam.dto.request;

import lombok.Data;

@Data
public class WebcamDataRequest {
    private String sessionId;
    private String frameData; // Base64 encoded
    private Long frameNumber;
}
