package org.example.mega_crew.domain.quiz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.quiz.dto.request.QuizRecordSaveRequestDto;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.domain.quiz.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/quiz")
@Tag(name="퀴즈 관련 controllers")
public class QuizController {
  private final QuizService quizService;

  public QuizController(QuizService quizService){
    this.quizService = quizService;
  }

  @PostMapping
  @Operation(summary = "퀴즈 생성", description = "선지 4개를 가진 퀴즈 5개를 생성합니다.")
  public List<QuizResponseDto> getQuiz() {
    final int QuizCount = 5; // magic number 방지, 변수로 관리
    return quizService.generateQuiz(QuizCount); // quiz 5개 생성
  }


  @PostMapping("/result")
  @Operation(summary = "USER별 퀴즈 기록", description = "USER별 정답 개수를 기록합니다.")
  public ResponseEntity<?> saveQuizResult(@RequestBody QuizRecordSaveRequestDto dto) {
    quizService.saveQuizRecord(dto);
    return ResponseEntity.ok().build();
  }


  // 디버깅용 GET 매핑
  @GetMapping("/test")
  @Operation(summary = "controller 디버깅용")
  public String test() {
    return "ok";
  }


  // 특정 날짜 특정 회원의 최고 정답 개수 조회
  @GetMapping("/correct-count/{date}/user/{userId}")
  public ResponseEntity<Integer> getUserMaxCorrectCount(
      @PathVariable String date,
      @PathVariable Long userId) {
    Integer maxCorrectCount = quizService.getUserMaxCorrectCount(date, userId);
    return ResponseEntity.ok(maxCorrectCount);
  }
}
