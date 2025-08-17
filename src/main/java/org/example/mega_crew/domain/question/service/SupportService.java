package org.example.mega_crew.domain.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.question.dto.request.AdminReplyRequestDto;
import org.example.mega_crew.domain.question.dto.request.SupportTicketRequestDto;
import org.example.mega_crew.domain.question.dto.response.SupportTicketResponseDto;
import org.example.mega_crew.domain.question.entity.SupportTicket;
import org.example.mega_crew.domain.question.entity.TicketCategory;
import org.example.mega_crew.domain.question.entity.TicketStatus;
import org.example.mega_crew.domain.question.repository.SupportTicketRepository;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.entity.UserRole;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupportService {

   private final SupportTicketRepository supportTicketRepository;
   private final UserRepository userRepository;

   // 관리자 권한 검증
   private User validateAdminRole(Long userId) throws AccessDeniedException {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

      if (user.getRole() != UserRole.ADMIN) {
         throw new AccessDeniedException("관리자 권한이 필요합니다.");
      }

      return user;
   }

   // 문의 제출
   public SupportTicketResponseDto createTicket(Long userId, SupportTicketRequestDto request) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

      TicketCategory category;
      try {
         category = TicketCategory.valueOf(request.getCategory().toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + request.getCategory());
      }

      SupportTicket ticket = SupportTicket.builder()
          .user(user)
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

   // 게시판용 - 모든 문의 조회 (공개 + 비공개)
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getAllTicketsForBoard(Pageable pageable, Long currentUserId) {
      Page<SupportTicket> tickets = supportTicketRepository.findAllTicketsForBoard(pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 게시판용 - 카테고리별 모든 문의 조회 (공개 + 비공개)
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getAllTicketsByCategory(String categoryStr, Pageable pageable, Long currentUserId) {
      TicketCategory category;
      try {
         category = TicketCategory.valueOf(categoryStr.toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + categoryStr);
      }

      Page<SupportTicket> tickets = supportTicketRepository.findAllTicketsByCategoryForBoard(category, pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 공개 문의 조회
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getPublicTickets(Pageable pageable) {
      Page<SupportTicket> tickets = supportTicketRepository.findPublicTickets(pageable);
      return tickets.map(SupportTicketResponseDto::fromPublic);
   }

   public SupportTicketResponseDto getTicketDetailWithPublicAccess(Long userId, Long ticketId) {
      SupportTicket ticket = supportTicketRepository.findById(ticketId)
          .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

      // 본인 글이거나 공개 글인 경우만 조회 허용
      if (!ticket.getUser().getId().equals(userId) && !ticket.getIsPublic()) {
         throw new IllegalArgumentException("접근 권한이 없습니다.");
      }

      return SupportTicketResponseDto.from(ticket);
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

   public SupportTicketResponseDto getMyTicketDetail(Long userId, Long ticketId) {
      SupportTicket ticket = supportTicketRepository.findByIdAndUserId(ticketId, userId)
          .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

      return SupportTicketResponseDto.from(ticket);
   }

   // 상태별 문의 조회 - 관리자
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getTicketsByStatus(TicketStatus status, Pageable pageable) {
      Page<SupportTicket> tickets = supportTicketRepository.findTicketsByStatus(status, pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 모든 문의 조회 (관리자용)
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getAllTickets(Long adminId, Pageable pageable) throws AccessDeniedException {
      validateAdminRole(adminId);

      Page<SupportTicket> tickets = supportTicketRepository.findAllTicketsForAdmin(pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 답변 대기 중인 문의 조회
   @Transactional(readOnly = true)
   public Page<SupportTicketResponseDto> getPendingTickets(Long adminId, Pageable pageable) throws AccessDeniedException {
      validateAdminRole(adminId);

      Page<SupportTicket> tickets = supportTicketRepository.findPendingTickets(pageable);
      return tickets.map(SupportTicketResponseDto::from);
   }

   // 특정 문의 상세 조회 (관리자용)
   @Transactional(readOnly = true)
   public SupportTicketResponseDto getTicketDetail(Long adminId, Long ticketId) throws AccessDeniedException {
      validateAdminRole(adminId);

      SupportTicket ticket = supportTicketRepository.findTicketById(ticketId)
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

      return SupportTicketResponseDto.from(ticket);
   }

   // 문의에 답변 작성 (요청된 엔드포인트 구조에 맞춤)
   public SupportTicketResponseDto addReply(Long adminId, Long ticketId, AdminReplyRequestDto request) throws AccessDeniedException {
      User admin = validateAdminRole(adminId);

      SupportTicket ticket = supportTicketRepository.findTicketById(ticketId)
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

      // 관리자 답변 추가
      ticket.addAdminResponse(request.getReply(), adminId);
      SupportTicket savedTicket = supportTicketRepository.save(ticket);

      log.info("관리자 답변 작성 완료 - 티켓 ID: {}, 관리자 ID: {}, 관리자명: {}",
          ticketId, adminId, admin.getUsername());

      return SupportTicketResponseDto.from(savedTicket);
   }

   // 문의 상태 변경
   public SupportTicketResponseDto updateTicketStatus(Long adminId, Long ticketId, TicketStatus status) throws AccessDeniedException {
      User admin = validateAdminRole(adminId);

      SupportTicket ticket = supportTicketRepository.findTicketById(ticketId)
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

      ticket.updateStatus(status);
      SupportTicket savedTicket = supportTicketRepository.save(ticket);

      log.info("문의 상태 변경 완료 - 티켓 ID: {}, 상태: {}, 관리자 ID: {}",
          ticketId, status, adminId);

      return SupportTicketResponseDto.from(savedTicket);
   }
}