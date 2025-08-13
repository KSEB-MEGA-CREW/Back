package org.example.mega_crew.domain.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.question.dto.request.SupportTicketRequestDto;
import org.example.mega_crew.domain.question.dto.response.SupportTicketResponseDto;
import org.example.mega_crew.domain.question.entity.SupportTicket;
import org.example.mega_crew.domain.question.entity.TicketCategory;
import org.example.mega_crew.domain.question.entity.TicketStatus;
import org.example.mega_crew.domain.question.repository.SupportTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupportService {

   private final SupportTicketRepository supportTicketRepository;

   // 문의 제출
   public SupportTicketResponseDto createTicket(Long userId, SupportTicketRequestDto request) {
      // 카테고리 변환
      TicketCategory category;
      try {
         category = TicketCategory.valueOf(request.getCategory().toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + request.getCategory());
      }

      SupportTicket ticket = SupportTicket.builder()
          .userId(userId)
          .userName(request.getUserName())
          .category(category)
          .subject(request.getSubject())
          .content(request.getContent())
          .isPublic(request.getIsPublic())
          .status(TicketStatus.PENDING)
          .build();

      SupportTicket savedTicket = supportTicketRepository.save(ticket);
      log.info("새 문의 티켓 생성 - ID: {}, 사용자: {}", savedTicket.getId(), userId);

      return SupportTicketResponseDto.from(savedTicket);
   }

   // 내 문의 조회
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getMyTickets(Long userId, Pageable pageable) {
      Page<SupportTicket> tickets = supportTicketRepository.findTicketsByUserId(userId, pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 공개 문의 조회
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getPublicTickets(Pageable pageable) {
      Page<SupportTicket> tickets = supportTicketRepository.findPublicTickets(pageable);
      return tickets.map(SupportTicketResponseDto::fromPublic);
   }

   // 카테고리별 공개 문의 조회
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getPublicTicketsByCategory(String categoryStr, Pageable pageable) {
      TicketCategory category;
      try {
         category = TicketCategory.valueOf(categoryStr.toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + categoryStr);
      }

      Page<SupportTicket> tickets = supportTicketRepository.findPublicTicketsByCategory(category, pageable);
      return tickets.map(SupportTicketResponseDto::fromPublic);
   }

   // 상태별 문의 조회 - 관리자
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getTicketsByStatus(TicketStatus status, Pageable pageable) {
      Page<SupportTicket> tickets = supportTicketRepository.findTicketsByStatus(status, pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }
}