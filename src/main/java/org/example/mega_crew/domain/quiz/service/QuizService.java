package org.example.mega_crew.domain.quiz.service;

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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

  private final QuizApiClient quizApiClient;
  private final QuizRecordRepository quizRecordRepository;
  private final QuizCategoryRecordRepository categoryRecordRepository;
  private final UserRepository userRepository;

  public QuizService(QuizApiClient quizApiClient, QuizRecordRepository quizRecordRepository, QuizCategoryRecordRepository categoryRecordRepository, UserRepository userRepository) {
    this.quizApiClient = quizApiClient;
    this.quizRecordRepository = quizRecordRepository;
    this.categoryRecordRepository = categoryRecordRepository;
    this.userRepository = userRepository;
  }


  // 문제 개수 5개, 선지 생성, 카테고리
  public List<QuizResponseDto> generateQuiz(int count) {
    List<Map<String, String>> wordList = Collections.emptyList();
    try {
      wordList = quizApiClient.fetchSignWords(count * 4);

      wordList = wordList.stream()
          // null 값 처리
          .filter(entry -> entry.containsKey("signDescription")
              && entry.get("signDescription") != null
              && !entry.get("signDescription").isEmpty())
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
    // 원본을 복사해 랜덤 추출
    List<Map<String, String>> pool = new ArrayList<>(wordList);
    List<QuizResponseDto> quizList = new ArrayList<>();
    Random rand = new Random();

    for (int i = 0; i < count && !pool.isEmpty(); i++) {
      // 랜덤하게 정답 하나 추출
      int answerIdx = rand.nextInt(pool.size());
      Map<String, String> answer = pool.remove(answerIdx);
      String answerWord = answer.get("word");
      String category = answer.get("category");

      // 오답 나온 단어 제외
      List<String> wrongWords = wordList.stream()
          .map(m -> m.get("word"))
          .filter(w -> !w.equals(answerWord))
          .collect(Collectors.toList());

      Collections.shuffle(wrongWords);
      List<ChoiceDto> choices = new ArrayList<>();
      choices.add(new ChoiceDto(answerWord, true));
      for (int k = 0; k < 3 && k < wrongWords.size(); k++) {
        choices.add(new ChoiceDto(wrongWords.get(k), false));
      }
      Collections.shuffle(choices);

      quizList.add(new QuizResponseDto(answer.get("signDescription"), category, choices));
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

  // 특정 날짜와 사용자의 최고 정답 개수 조회
  public Integer getUserMaxCorrectCount(String date, Long userId) {
    LocalDate localDate = LocalDate.parse(date);
    return quizRecordRepository.getMaxCorrectCountByDateAndUser(localDate, userId);
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