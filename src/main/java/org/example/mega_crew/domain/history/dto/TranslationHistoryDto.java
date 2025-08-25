package org.example.mega_crew.domain.history.dto;

import lombok.*;
import org.example.mega_crew.domain.history.entity.WorkType;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
public class TranslationHistoryDto {
   private Long id;
   private Long userId;
   private WorkType workType;
   private String inputContent;
   private String outputContent;
   private String processingStatus;
   private Integer processingTime;
   private String errorMessage;
   private String userAgent;
   private Integer inputLength;
   private LocalDateTime createdDate;
   private LocalDateTime modifiedDate;
   private LocalDateTime expiresAt;
   private Boolean isExpired;

   private String feedback;
   private String translatedText;
   private String translatedTime;
   private LocalDateTime feedbackSubmittedAt;
}
