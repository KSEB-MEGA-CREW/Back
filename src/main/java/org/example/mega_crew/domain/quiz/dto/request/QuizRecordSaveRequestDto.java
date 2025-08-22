package org.example.mega_crew.domain.quiz.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class QuizRecordSaveRequestDto {

   private Long userId;
   private int correctCount;
   private Map<String, Integer> categoryCorrectCounts;
   private List<IncorrectQuizDto> incorrectAnswers;
}
