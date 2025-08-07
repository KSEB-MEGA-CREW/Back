package org.example.mega_crew.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mega_crew.domain.quiz.dto.choice.ChoiceDto;

import java.util.List;

// DB에서 받아오는 값: 수어 설명, 선지, 카테고리
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto { // 프론트와 변수명 일치
  private String word;
  private String signDescription;
  private String subDescription;
  private String category;
  private List<ChoiceDto> choices;

  public QuizResponseDto(String signDescription, String subDescription, String category, List<ChoiceDto> choices) {
  }
}
