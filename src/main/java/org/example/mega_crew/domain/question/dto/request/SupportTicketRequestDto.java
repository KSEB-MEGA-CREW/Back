package org.example.mega_crew.domain.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupportTicketRequestDto {

   @NotBlank(message = "사용자명은 필수입니다.")
   @Size(min = 2, max = 20, message = "사용자명은 2자 이상 20자 이하여야 합니다.")
   private String userName;

   @NotBlank(message = "카테고리는 필수입니다.")
   private String category;

   @NotBlank(message = "제목은 필수입니다.")
   @Size(min = 2, max = 200, message = "제목은 5자 이상 200자 이하여야 합니다.")
   private String subject;

   @NotBlank(message = "내용은 필수입니다.")
   @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
   private String content;

   @NotNull(message = "공개 여부는 필수입니다.")
   private Boolean isPublic;

   public SupportTicketRequestDto(String userName, String category, String subject, String content, Boolean isPublic) {
      this.userName = userName;
      this.category = category;
      this.subject = subject;
      this.content = content;
      this.isPublic = isPublic;
   }
}
