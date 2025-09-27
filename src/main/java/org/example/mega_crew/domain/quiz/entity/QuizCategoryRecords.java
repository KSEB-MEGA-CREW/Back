package org.example.mega_crew.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;
// 조회 성능 향상을 위한 인덱스 추가
@Entity
@Table(name = "quiz_category_records",
indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_user_category", columnList = "user_id, category")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class QuizCategoryRecords extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false)
   private String category;

   @Column(nullable = false)
   private int correctCount;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id")
   private User user;

   public QuizCategoryRecords(String category, int correctCount, User user) {
      this.category = category;
      this.correctCount = correctCount;
      this.user = user;
   }
}