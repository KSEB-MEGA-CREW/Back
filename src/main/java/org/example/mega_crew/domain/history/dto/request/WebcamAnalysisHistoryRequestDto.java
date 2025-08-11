package org.example.mega_crew.domain.history.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WebcamAnalysisHistoryRequestDto {
   @NotNull
   private Long userId;

   @NotBlank
   private String fileName;

   private String userAgent;
   private String clientIp;
}
