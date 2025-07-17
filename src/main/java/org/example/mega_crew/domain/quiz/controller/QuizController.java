package org.example.mega_crew.domain.quiz.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.quiz.dto.request.QuizRequestDto;
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
  public List<QuizResponseDto> getQuiz(@RequestBody QuizRequestDto request) {
    System.out.println("=== QuizController.getQuiz() 호출됨 ===");
    return quizService.generateQuiz(request.getCount());
  }

  // 디버깅용 GET 매핑
  @GetMapping("/test")
  public String test() {
    System.out.println("=== GET /api/quiz/test 호출됨 ===");
    return "ok";
  }
}
