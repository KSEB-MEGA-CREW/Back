package org.example.mega_crew.domain.question.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.mega_crew.domain.question.entity.SupportTicket;
import org.example.mega_crew.domain.question.entity.TicketCategory;
import org.example.mega_crew.domain.question.entity.TicketStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class SupportTicketResponseDto {

   private Long id;
   private String userName;
   private TicketCategory category;
   private Long userId;
   private String subject;
   private String content;
   private Boolean isPublic;
   private TicketStatus status;
   private String adminResponse;
   private LocalDateTime createdDate;
   private LocalDateTime modifiedDate;

   public static SupportTicketResponseDto from(SupportTicket ticket) {
      return SupportTicketResponseDto.builder()
          .id(ticket.getId())
          .userId(ticket.getUser().getId())
          .userName(ticket.getUserName())
          .category(ticket.getCategory())
          .subject(ticket.getSubject())
          .content(ticket.getContent())
          .isPublic(ticket.getIsPublic())
          .status(ticket.getStatus())
          .adminResponse(ticket.getAdminResponse())
          .createdDate(ticket.getCreatedDate())
          .modifiedDate(ticket.getModifiedDate())
          .build();
   }

   // 공개 문의용 (개인정보 제외)
   public static SupportTicketResponseDto fromPublic(SupportTicket ticket) {
      return SupportTicketResponseDto.builder()
          .id(ticket.getId())
          .userName("***") // 익명 처리
          .category(ticket.getCategory())
          .subject(ticket.getSubject())
          .content(ticket.getContent())
          .isPublic(ticket.getIsPublic())
          .status(ticket.getStatus())
          .adminResponse(ticket.getAdminResponse())
          .createdDate(ticket.getCreatedDate())
          .modifiedDate(ticket.getModifiedDate())
          .build();
   }
}
