package org.example.mega_crew.domain.quiz.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.domain.quiz.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/quiz")
public class QuizController {
  private final QuizService quizService;

  public QuizController(QuizService quizService){
    this.quizService = quizService;
  }

  @PostMapping
  public List<QuizResponseDto> getQuiz() {
    final int QuizCount = 5; // magic number 방지, 변수로 관리
    return quizService.generateQuiz(QuizCount); // quiz 5개 생성
  }

  // 디버깅용 GET 매핑
  @GetMapping("/test")
  public String test() {
    return "ok";
  }
}
