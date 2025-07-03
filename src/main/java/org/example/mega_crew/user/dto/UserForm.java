package org.example.mega_crew.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.mega_crew.user.entity.User;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UserForm {
  private Long user_id;
  private String email;
  private String password;
  private String hearing_status;

  public User toEntity(){
   return new User(user_id, email, password, hearing_status);
  }
}
