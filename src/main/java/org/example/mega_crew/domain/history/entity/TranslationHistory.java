package org.example.mega_crew.domain.history.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "translation_histories")
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
}
