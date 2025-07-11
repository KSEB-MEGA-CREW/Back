package org.example.mega_crew.domain.webcam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebcamSessionListResponse {
    private String sessionId;
    private String status;
    private String thumbnailUrl;
    private Long duration;
    private LocalDateTime createdDate;
}
