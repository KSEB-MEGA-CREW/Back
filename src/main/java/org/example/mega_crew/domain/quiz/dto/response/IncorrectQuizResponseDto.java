package org.example.mega_crew.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mega_crew.domain.quiz.entity.IncorrectQuizRecords;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncorrectQuizResponseDto {
   private Long id;
   private String word;
   private String category;
   private String signDescription;
   private String subDescription;
   private LocalDateTime createdDate;

   public static IncorrectQuizResponseDto from(IncorrectQuizRecords entity) {
      return new IncorrectQuizResponseDto(
          entity.getId(),
          entity.getWord(),
          entity.getCategory(),
          entity.getSignDescription(),
          entity.getSubDescription(),
          entity.getCreatedDate()
      );
   }
}