package org.example.mega_crew.domain.quiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.quiz.dto.choice.ChoiceDto;
import org.example.mega_crew.domain.quiz.dto.request.QuizRecordSaveRequestDto;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.domain.quiz.entity.QuizCategoryRecords;
import org.example.mega_crew.domain.quiz.entity.QuizRecords;
import org.example.mega_crew.domain.quiz.repository.QuizCategoryRecordRepository;
import org.example.mega_crew.domain.quiz.repository.QuizRecordRepository;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.example.mega_crew.global.client.api.quiz.QuizApiClient;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class QuizService {

  private final QuizApiClient quizApiClient;
  private final QuizRecordRepository quizRecordRepository;
  private final QuizCategoryRecordRepository categoryRecordRepository;
  private final UserRepository userRepository;

  // 문제 개수 5개, 선지 생성, 카테고리
  public List<QuizResponseDto> generateQuiz(int count, Long userId) {
    List<Map<String,String>> wordList = Collections.emptyList();
    try {
      wordList = quizApiClient.fetchSignWords(count*4);

      wordList = wordList.stream()
          // null 값 처리
          .filter(entry -> entry.containsKey("signDescription")
              && entry.get("signDescription") != null
              && !entry.get("signDescription").isEmpty())
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("퀴즈 API 호출 실패", e);
      throw new RuntimeException("퀴즈 데이터를 가져오는데 실패했습니다.",e);
    }
    List<QuizResponseDto> quizList = generateQuizFromWordList(wordList, count);

    log.info("퀴즈 생성 완료 - 사용자 ID: {}", userId);

    return quizList;
  }

  private List<QuizResponseDto> generateQuizFromWordList(List<Map<String,String>> wordList, int count){
    List<Map<String,String>> pool = new ArrayList<>(wordList);
    List<QuizResponseDto> quizList = new ArrayList<>();
    Random rand = new Random();

    for (int i = 0; i < count && !pool.isEmpty(); i++) {
      int answerIdx = rand.nextInt(pool.size());
      Map<String, String> answer = pool.remove(answerIdx);
      String answerWord = answer.get("word");
      String answerMeaning = answer.get("meaning"); // meaning 추가 => 프론트에서 써서 추가했는데 나중에 프론트랑 얘기해서 제거 가능
      String category = answer.get("category");

      List<Map<String,String>> wrongWords = wordList.stream()
                      .filter(w -> !w.get("word").equals(answerWord))
                      .collect(Collectors.toList());

      Collections.shuffle(wrongWords);
      List<ChoiceDto> choices = new ArrayList<>();

      choices.add(new ChoiceDto(answerWord, answerMeaning , true));

      for (int k = 0; k < 3 && k < wrongWords.size(); k++) {
        Map<String,String> wrongChoice = wrongWords.get(k);
        choices.add(new ChoiceDto(
                wrongChoice.get("word"),
                wrongChoice.get("meaning"),
                false
        ));
      }
      Collections.shuffle(choices);

      quizList.add(new QuizResponseDto(answer.get("signDescription"), answerWord, category, choices));
    }
    return quizList;
  }

  // quiz 정답 개수 저장
  public void saveQuizRecord(QuizRecordSaveRequestDto dto) {
    User user = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + dto.getUserId()));
    QuizRecords record = new QuizRecords(dto.getCorrectCount(), user);
    quizRecordRepository.save(record);

    // 카테고리별 기록도 함께 저장
    saveCategoryQuizRecord(dto);
  }

  // 특정 월의 사용자 일별 퀴즈 정답률 조회
  public Map<String, Double> getUserMonthlyQuizStats(int year, int month, Long userId) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    List<Object[]> results = quizRecordRepository.getMonthlyQuizStatsByUser(startDate, endDate, userId);

    Map<String, Double> monthlyStats = new HashMap<>();

    // 당월의 모든 날짜 기본 값 0.0으로 초기화
    for (int day = 1; day <= startDate.lengthOfMonth(); day++) {
      LocalDate date = LocalDate.of(year, month, day);
      monthlyStats.put(date.toString(), 0.0);
    }

    // 실제 데이터로 정답률 계산
    for (Object[] result : results) {
      LocalDate date = (LocalDate) result[0];
      Long totalCorrect = (Long) result[1];
      Long totalQuestions = (Long) result[2];

      double accuracy = totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0.0;
      monthlyStats.put(date.toString(), Math.round(accuracy * 10) / 10.0); // 소수점 1자리
    }

    return monthlyStats;
  }

  // 카테고리별 정답 개수 저장
  public void saveCategoryQuizRecord(QuizRecordSaveRequestDto dto) {
    User user = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + dto.getUserId()));

    if (dto.getCategoryCorrectCounts() != null) {
      dto.getCategoryCorrectCounts().forEach((category, count) -> {
        if (count > 0) { // 정답이 있는 카테고리만 저장
          QuizCategoryRecords categoryRecords = new QuizCategoryRecords(category, count, user);
          categoryRecordRepository.save(categoryRecords);
        }
      });
    }
  }

  public Map<String, Integer> getCategoryStatsByUser(Long userId) {
    List<Object[]> results = categoryRecordRepository.getCategoryStatsByUser(userId);

    // 기본값 0 설정
    Map<String, Integer> stats = new HashMap<>();

    stats.put("개념", 0); stats.put("경제생활", 0); stats.put("교육", 0); stats.put("기타", 0);
    stats.put("나라명 및 지명", 0); stats.put("동식물", 0); stats.put("문화", 0); stats.put("사회생활", 0);
    stats.put("삶", 0); stats.put("식생활", 0); stats.put("의생활", 0); stats.put("인간", 0);
    stats.put("자연", 0); stats.put("정치와 행정", 0); stats.put("종교", 0); stats.put("주생활", 0);

    // 실제 값
    for (Object[] result : results) {
      String category = (String) result[0];
      Long count = (Long) result[1];
      stats.put(category, count.intValue());
    }

    return stats;
  }
}