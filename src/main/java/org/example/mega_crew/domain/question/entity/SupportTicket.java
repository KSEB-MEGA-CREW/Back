package org.example.mega_crew.domain.question.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SupportTicket extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", referencedColumnName = "id")
   private User user;

   @Column(nullable = false)
   private String userName;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private TicketCategory category;

   @Column(nullable = false, length = 200)
   private String subject;

   @Column(nullable = false, columnDefinition = "TEXT")
   private String content;

   @Column(nullable = false)
   private Boolean isPublic;

   @Enumerated(EnumType.STRING)
   @Builder.Default
   private TicketStatus status = TicketStatus.PENDING;

   @Column(columnDefinition = "TEXT")
   private String adminResponse;

   @Column
   private LocalDateTime lastEditedDate;  // 사용자가 마지막으로 수정한 날짜

   @Column
   private LocalDateTime adminResponseDate;  // 관리자 답변 날짜

   private Long respondedBy;

   // 상태 업데이트 메서드
   public void updateStatus(TicketStatus status) {
      this.status = status;
   }

   // 게시글 수정
   public void updateTicket(String subject, String content, TicketCategory category, Boolean isPublic) {
      if (subject != null && !subject.trim().isEmpty()) {
         this.subject = subject;
      }
      if (content != null && !content.trim().isEmpty()) {
         this.content = content;
      }
      if (category != null) {
         this.category = category;
      }
      if (isPublic != null) {
         this.isPublic = isPublic;
      }
      this.lastEditedDate = LocalDateTime.now();
   }

   // 관리자 답변 추가
   public void addAdminResponse(String response, Long adminId) {
      this.adminResponse = response;
      this.respondedBy = adminId;
      this.status = TicketStatus.ANSWERED;
      this.adminResponseDate = LocalDateTime.now();
   }
}
