package org.example.mega_crew.domain.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupportTicketUpdateRequestDto {

   @NotBlank
   private String category;

   @NotBlank(message = "제목은 필수입니다.")
   private String subject;

   @NotBlank(message = "내용은 필수입니다.")
   private String content;

   private Boolean isPublic;
}
