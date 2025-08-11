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
   private String clientIp;
   private Integer inputLength;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
