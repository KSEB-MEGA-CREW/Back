package org.example.mega_crew.domain.quiz.service;

import org.example.mega_crew.domain.quiz.dto.choice.ChoiceDto;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.global.client.api.quiz.QuizApiClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// 퀴즈 생성
@Service
public class QuizService {
  private final QuizApiClient quizApiClient;

  public QuizService(QuizApiClient quizApiClient) {
    this.quizApiClient = quizApiClient;
  }

  // 문제 개수 5개로 선지 생성
  public List<QuizResponseDto> generateQuiz(int count) {
    List<Map<String, String>> wordList = Collections.emptyList();
    try {
      wordList = quizApiClient.fetchSignWords(count * 4);

      wordList = wordList.stream()
          // ""로 나오는 null 값 처리
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

      // 오답 후보: 나온 단어 제외
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

      quizList.add(new QuizResponseDto(answer.get("signDescription"), choices));
    }
    return quizList;
  }
}

