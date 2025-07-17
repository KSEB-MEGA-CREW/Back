package org.example.mega_crew.domain.quiz.service;

import org.example.mega_crew.domain.quiz.dto.choice.ChoiceDto;
import org.example.mega_crew.domain.quiz.dto.response.QuizResponseDto;
import org.example.mega_crew.global.client.api.quiz.QuizApiClient;
import org.springframework.stereotype.Service;

import java.util.*;

// 퀴즈 생성
@Service
public class QuizService {
  private final QuizApiClient quizApiClient;

  public QuizService(QuizApiClient quizApiClient){
    this.quizApiClient = quizApiClient;
  }

  // 문제 개수 5개로 선지 생성
  public List<QuizResponseDto> generateQuiz(int count){
    System.out.println("=== QuizService.generateQuiz() 호출됨 ===");
    List<Map<String, String>> wordList = Collections.emptyList();
    try {
      wordList = quizApiClient.fetchSignWords(count * 4);
    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
    Collections.shuffle(wordList);
    List<QuizResponseDto> quizList = new ArrayList<>();

    // 선지중에 정답과 오답 생성
    for (int i = 0; i < count; i++) {
      Map<String, String> answer = wordList.get(i);
      Set<String> used = new HashSet<>();
      used.add(answer.get("word"));

      List<ChoiceDto> choices = new ArrayList<>();
      choices.add(new ChoiceDto(answer.get("word"), true));

      int j = 0, k = 0;
      while (j < wordList.size() && k < 3) {
        String wrong = wordList.get(j).get("word");
        if (!used.contains(wrong)) {
          choices.add(new ChoiceDto(wrong, false));
          used.add(wrong);
          k++;
        }
        j++;
      }
      Collections.shuffle(choices);

      quizList.add(new QuizResponseDto(answer.get("signDescription"), choices));
    }
    return quizList;
  }
}
