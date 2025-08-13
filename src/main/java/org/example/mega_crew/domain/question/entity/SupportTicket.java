package org.example.mega_crew.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

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

   private Long respondedBy;

   // 상태 업데이트 메서드
   public void updateStatus(TicketStatus status) {
      this.status = status;
   }

   // 관리자 답변 추가
   public void addAdminResponse(String response, Long adminId) {
      this.adminResponse = response;
      this.respondedBy = adminId;
      this.status = TicketStatus.ANSWERED;
   }
}
