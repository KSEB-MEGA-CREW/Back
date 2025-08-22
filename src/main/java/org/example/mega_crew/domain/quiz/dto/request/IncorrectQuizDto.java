package org.example.mega_crew.domain.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncorrectQuizDto {

   private String word;
   private String meaning;
   private String category;
   private String signDescription;
   private String subDescription;
   private String userAnswer;
}
