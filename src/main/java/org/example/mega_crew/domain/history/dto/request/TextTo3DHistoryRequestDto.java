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
public class TextTo3DHistoryRequestDto {

   @NotNull
   private Long userId;

   @NotBlank
   private String textContent;

   private String userAgent;
}
