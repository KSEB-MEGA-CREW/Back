package org.example.mega_crew.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSignupRequest {

   @NotBlank(message = "이메일은 필수입니다!")
   @Email(message = "올바른 이메일 형식이 아닙니다.")
   private String email;

   @NotBlank(message = "비밀번호는 필수입니다.")
   @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
       message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.")
   private String password;

   @NotBlank(message = "관리자명은 필수입니다.")
   @Size(min = 2, max = 20, message = "관리자명은 2자 이상 20자 이하여야 합니다.")
   private String username;

   @NotBlank(message = "관리자 인증 코드는 필수입니다.")
   private String adminCode; // 관리자 인증 코드

   public AdminSignupRequest(String email, String password, String username, String adminCode) {
      this.email = email;
      this.password = password;
      this.username = username;
      this.adminCode = adminCode;
   }
}
