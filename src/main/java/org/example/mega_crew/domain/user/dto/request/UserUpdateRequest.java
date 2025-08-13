package org.example.mega_crew.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

   @NotBlank(message = "이름은 필수입니다.")
   @Size(min=2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
   private String username;

   @NotBlank(message = "청각상태는 필수입니다.")
   private String hearing;

   public UserUpdateRequest(String username, String hearing) {
      this.username = username;
      this.hearing = hearing;
   }
}
