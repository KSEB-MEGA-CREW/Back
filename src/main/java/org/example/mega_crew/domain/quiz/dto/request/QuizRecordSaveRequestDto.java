package org.example.mega_crew.domain.quiz.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizRecordSaveRequestDto {

   private  int correctCount;
   private Long userId;
}
