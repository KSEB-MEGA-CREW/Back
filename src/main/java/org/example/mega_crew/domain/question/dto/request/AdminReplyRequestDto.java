package org.example.mega_crew.domain.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReplyRequestDto {

   @NotBlank(message = "답변 내용은 필수입니다.")
   @Size(min = 10, max = 2000, message = "답변은 10자 이상 2000자 이하여야 합니다.")
   private String reply;

   public AdminReplyRequestDto(String reply) {
      this.reply = reply;
   }
}
