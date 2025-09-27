package org.example.mega_crew.domain.history.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

import java.time.LocalDateTime;
// 조회 성능 향상을 위해 인덱스 생성
// 집계 쿼리 성능 향상을 위해 worktype별 작업 횟수를 저장하는 column 생성 고려
// => 현재로선 인덱스로 충분해 보임, 추후 APM 연결 후 관찰하여 판단하기
@Entity
@Table(name = "translation_histories",
indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_user_is_expired", columnList = "user_id, is_expired"),
        @Index(name = "idx_user_work_type", columnList = "user_id, work_type")
})
@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationHistory extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", referencedColumnName = "id")
   private User user;

   @Enumerated(EnumType.STRING)
   @Column(name="work_type", nullable = false)
   private WorkType workType;

   @Column(name="input_content")
   private String inputContent;

   @Column(name="output_content")
   private String outputContent;

   @Column(name = "processing_status")
   private String processingStatus;

   @Column(name = "processing_time")
   private Integer processingTime;

   @Column(name = "error_message")
   private String errorMessage;

   @Column(name = "user_agent")
   private String userAgent;

   @Column(name = "input_length")
   private Integer inputLength;

   @Column(name = "expires_at")
   private LocalDateTime expiresAt;

   @Column(name = "is_expired", nullable = false)
   @Builder.Default
   private Boolean isExpired = false;

   @Column(name = "feedback")
   private String feedback;

   @Column(name = "translated_text")
   private String translatedText;

   @Column(name = "translated_time")
   private String translatedTime;

   @Column(name = "feedback_submitted_at")
   private LocalDateTime feedbackSubmittedAt;


   // 만료 기간 설정
   @PrePersist
   public void setExpirationDate() {
      if (this.expiresAt == null) {
         this.expiresAt = LocalDateTime.now().plusDays(30); // 30일
      }
   }

   // 만료 여부 확인
   public boolean checkExpired() {
      return this.isExpired || (this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt));
   }

   // 수동 만료 처리
   public void markAsExpired() {
      this.isExpired = true;
   }

   // 번역 평가 업데이트
   public void updateFeedback(String feedback, String translatedText, String translatedTime) {
      this.feedback = feedback;
      this.translatedText = translatedText;
      this.translatedTime = translatedTime;
      this.feedbackSubmittedAt = LocalDateTime.now();
   }

   // 평가 여부 확인
   public boolean hasFeedback() {
      return this.feedback != null && this.feedbackSubmittedAt != null;
   }
}
