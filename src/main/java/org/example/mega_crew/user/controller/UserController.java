package org.example.mega_crew.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.user.entity.User;
import org.example.mega_crew.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/userLogin")
  public String newUserForm(){
    return "userLogin";
  }


}
