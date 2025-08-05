package org.example.mega_crew.domain.quiz.dto.choice;

import lombok.AllArgsConstructor;
import lombok.Getter;

// client가 선택하는 정답, 정답 여부
@Getter
@AllArgsConstructor
public class ChoiceDto { // 프론트와 일치
  private String word;
  private String meaning;
  private boolean answer;
}
