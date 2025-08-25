package org.example.mega_crew.domain.history.dto.response;

import lombok.*;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransHistoryResponseDto {

   private Long historyId;
   private String feedback;
   private String translatedText;
   private String translatedTime;
   private String status;
   private String message;
}