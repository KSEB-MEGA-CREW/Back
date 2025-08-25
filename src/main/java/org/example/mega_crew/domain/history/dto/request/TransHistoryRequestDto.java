package org.example.mega_crew.domain.history.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransHistoryRequestDto {

   @NotNull
   private Long historyId;

   @NotBlank
   @Pattern(regexp = "^(good|bad)$", message = "feedback must be either 'good' or 'bad'")
   private String feedback;

   @NotBlank
   private String translatedText;

   @NotBlank
   private String translatedTime; // ISO string format

   private Long userId;
}