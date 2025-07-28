package org.example.mega_crew.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
public class QuizRecord extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private int correctCount;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id")
   private User user;

   public QuizRecord() {}

   public QuizRecord(int correctCount, User user) {
      this.correctCount = correctCount;
      this.user = user;
   }
}
