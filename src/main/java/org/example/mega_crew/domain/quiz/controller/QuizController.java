package org.example.mega_crew.domain.quiz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.quiz.dto.request.QuizRecordSaveRequestDto;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.domain.quiz.service.QuizService;
import org.example.mega_crew.domain.user.service.UserService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.example.mega_crew.global.utility.AuthenticationHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name="퀴즈 관련 controllers")
@Slf4j // log 사용
public class QuizController {
  private final QuizService quizService;
  private final JwtUtil jwtUtil;
  private final UserService userService;
  private final AuthenticationHelper authenticationHelper;

  @PostMapping
  @Operation(summary = "퀴즈 생성", description = "선지 4개를 가진 퀴즈 5개를 생성합니다.")
  public ResponseEntity<ApiResponse<List<QuizResponseDto>>> getQuiz(HttpServletRequest request) {
    try{
      // JWT 에서 사용자 정보 추출
      Long userId = authenticationHelper.extractUserIdFromRequest(request);

      // 사용자 인증 확인 로그
      log.info("퀴즈 생성 요청 - 사용자 ID : {}", userId);

      final int QuizCount = 5; // magic number 방지, 변수로 관리
      List<QuizResponseDto> quizList = quizService.generateQuiz(QuizCount, userId);

      log.debug("퀴즈 생성 완료 - 사용자 ID: {}",userId);

      return ResponseEntity.ok(ApiResponse.success(quizList));
    } catch (UsernameNotFoundException e){ // 사용자 인증 에러 처리 추가
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("인증되지 않은 사용자입니다. :"+e.getMessage()));
    }catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ApiResponse.error("퀴즈 생성 실패: "+e.getMessage()));
    }
  }


  @PostMapping("/result")
  @Operation(summary = "사용자별 퀴즈 기록", description = "사용자별 정답 개수를 기록합니다.")
  public ResponseEntity<?> saveQuizResult(@RequestBody QuizRecordSaveRequestDto dto
  , HttpServletRequest request) {
    // 사용자 인증 처리 추가
    try{
      authenticationHelper.validateUserAccess(request, dto.getUserId());

      quizService.saveQuizRecord(dto);
      return ResponseEntity.ok(ApiResponse.success("저장 완료"));
    } catch(Exception e){
      log.error("퀴즈 결과 저장 실패", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ApiResponse.error("저장 실패: "+e.getMessage()));
    }

  }


  // 특정 월의 사용자 일별 퀴즈 정답률 조회
  @GetMapping("/quiz-stats/monthly/{year}/{month}/user/{userId}")
  @Operation(summary = "특정 월의 사용자 일별 퀴즈 통계 조회")
  public ResponseEntity<Map<String, Double>> getUserMonthlyQuizStats(
      @PathVariable int year,
      @PathVariable int month,
      @PathVariable Long userId,
      HttpServletRequest request) {

    authenticationHelper.validateUserAccess(request, userId);

    Map<String, Double> monthlyStats = quizService.getUserMonthlyQuizStats(year, month, userId);
    return ResponseEntity.ok(monthlyStats);
  }


  // 일단 놔뒀습니다, 불필요할 시 추후 삭제 필요
  @GetMapping("/category-stats/user/{userId}")
  @Operation(summary = "사용자별 카테고리별 통계 조회")
  public ResponseEntity<Map<String, Integer>> getCategoryStatsByUser(
      @PathVariable Long userId,
      HttpServletRequest request) {

    authenticationHelper.validateUserAccess(request, userId);

    Map<String, Integer> stats = quizService.getCategoryStatsByUser(userId);
    return ResponseEntity.ok(stats);
  }
}


