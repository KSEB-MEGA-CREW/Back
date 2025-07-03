package org.example.mega_crew.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.user.entity.User;
import org.example.mega_crew.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {

  @Autowired
  private UserService userService;


}
