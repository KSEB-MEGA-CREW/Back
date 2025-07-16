package org.example.mega_crew.domain.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// clinet 요청: 문제 개수 고르기
@Getter
@Setter
@NoArgsConstructor
public class QuizRequestDto {
  @NotBlank(message = "문제개수를 선택해주세요.")
  private int count;
}
