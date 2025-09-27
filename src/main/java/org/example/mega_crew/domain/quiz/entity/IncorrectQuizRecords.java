package org.example.mega_crew.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;
// 조회 성능 향상을 위한 인덱스 생성
@Entity
@Table(name = "incorrect_quiz_records",
indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
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
   private String category;

   @Column(nullable = false, columnDefinition = "TEXT")
   private String signDescription;

   @Column(columnDefinition = "TEXT")
   private String subDescription;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id")
   private User user;

   public IncorrectQuizRecords(String word, String category,
                               String signDescription, String subDescription,
                               User user) {
      this.word = word;
      this.category = category;
      this.signDescription = signDescription;
      this.subDescription = subDescription;
      this.user = user;
   }
}
