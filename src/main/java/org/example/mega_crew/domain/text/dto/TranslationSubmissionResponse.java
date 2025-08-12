package org.example.mega_crew.domain.text.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranslationSubmissionResponse {
    private String status; // "SUBMITTED", "FAILED", "VALIDATION_ERROR"
    private String requestId; // 추적용 UUID
    private Long submissionTime;
}
