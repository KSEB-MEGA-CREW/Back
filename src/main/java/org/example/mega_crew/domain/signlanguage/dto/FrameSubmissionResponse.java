package org.example.mega_crew.domain.signlanguage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FrameSubmissionResponse { // 프레임 전송 단순 상태 응답을 위한 dto
    private String status; // "SUBMITTED", "FAILED", "VALIDATION_ERROR"
    private String requestId; // 추적용 UUID
    private Long submissionTime;
}
