package org.example.mega_crew.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

@Entity
@Table(name = "incorrect_quiz_records")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncorrectQuizRecords extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false)
   private String word;

   @Column(nullable = false)
   private String meaning;

   @Column(nullable = false)
   private String category;

   @Column(nullable = false, columnDefinition = "TEXT")
   private String signDescription;

   @Column(columnDefinition = "TEXT")
   private String subDescription;

   @Column(nullable = false)
   private String userAnswer;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id")
   private User user;

   public IncorrectQuizRecords(String word, String meaning, String category,
                               String signDescription, String subDescription,
                               String userAnswer, User user) {
      this.word = word;
      this.meaning = meaning;
      this.category = category;
      this.signDescription = signDescription;
      this.subDescription = subDescription;
      this.userAnswer = userAnswer;
      this.user = user;
   }
}
