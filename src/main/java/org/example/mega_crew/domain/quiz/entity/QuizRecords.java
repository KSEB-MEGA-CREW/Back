package org.example.mega_crew.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;
// 월별 조회를 제공하므로 user_id + created_date 조합 활용
@Entity
@Table(name = "quiz_records",
indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_user_created_date", columnList = "user_id, created_date")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizRecords extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private int correctCount;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", referencedColumnName = "id")
   private User user;

   public QuizRecords(int correctCount, User user) {
      this.correctCount = correctCount;
      this.user = user;
   }
}
