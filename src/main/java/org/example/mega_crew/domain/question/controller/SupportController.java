package org.example.mega_crew.domain.question.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.question.dto.request.AdminReplyRequestDto;
import org.example.mega_crew.domain.question.dto.request.SupportTicketRequestDto;
import org.example.mega_crew.domain.question.dto.response.SupportTicketResponseDto;
import org.example.mega_crew.domain.question.entity.TicketStatus;
import org.example.mega_crew.domain.question.service.SupportService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

   private final SupportService supportService;
   private final JwtUtil jwtUtil;

   // 문의 제출
   @PostMapping("/ticket")
   public ResponseEntity<ApiResponse<SupportTicketResponseDto>> createTicket(
       @Valid @RequestBody SupportTicketRequestDto request,
       HttpServletRequest httpRequest) {

      String token = jwtUtil.extractTokenFromRequest(httpRequest);
      Long userId = jwtUtil.extractUserId(token);

      SupportTicketResponseDto response = supportService.createTicket(userId, request);

      return ResponseEntity.ok(ApiResponse.success(response));
   }

   // 내 문의 목록 조회
   @GetMapping("/my-tickets")
   public ResponseEntity<ApiResponse<Page<SupportTicketResponseDto>>> getMyTickets(
       @RequestParam(defaultValue = "1") int page,
       @RequestParam(defaultValue = "5") int size,
       HttpServletRequest httpRequest) {

      String token = jwtUtil.extractTokenFromRequest(httpRequest);
      Long userId = jwtUtil.extractUserId(token);

      Pageable pageable = PageRequest.of(page - 1, size);
      Page<SupportTicketResponseDto> tickets = supportService.getMyTickets(userId, pageable);

      return ResponseEntity.ok(ApiResponse.success(tickets));
   }

   // 게시판 조회(모든 문의 반환 - 공개+비공개)
   @GetMapping("/public")
   public ResponseEntity<ApiResponse<Page<SupportTicketResponseDto>>> getPublicTickets(
       @RequestParam(defaultValue = "1") int page,
       @RequestParam(defaultValue = "5") int size,
       @RequestParam(required = false) String category,
       HttpServletRequest httpRequest) {

      log.info(" 게시판 조회 요청 - page: {}, size: {}", page, size);

      // 현재 사용자 ID 추출 (로그인한 경우)
      Long currentUserId = null;
      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         if (token != null) {
            currentUserId = jwtUtil.extractUserId(token);
         }
      } catch (Exception e) {
         log.debug("토큰 추출 실패 (로그인하지 않은 사용자): {}", e.getMessage());
      }

      Pageable pageable = PageRequest.of(page - 1, size);
      Page<SupportTicketResponseDto> tickets;

      if (category != null && !category.trim().isEmpty()) {
         tickets = supportService.getAllTicketsByCategory(category, pageable, currentUserId);
      } else {
         tickets = supportService.getAllTicketsForBoard(pageable, currentUserId);
      }

      log.info(" 조회 결과 - 총 {}개, 현재 페이지 {}개",
          tickets.getTotalElements(), tickets.getContent().size());

      return ResponseEntity.ok(ApiResponse.success(tickets));
   }

   @PostMapping("/tickets/{ticketId}/reply")
   public ResponseEntity<ApiResponse<SupportTicketResponseDto>> addReply(
       @PathVariable Long ticketId,
       @Valid @RequestBody AdminReplyRequestDto request,
       HttpServletRequest httpRequest) {

      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         Long adminId = jwtUtil.extractUserId(token);

         SupportTicketResponseDto response = supportService.addReply(adminId, ticketId, request);

         return ResponseEntity.ok(ApiResponse.success(response));
      } catch (AccessDeniedException e) {
         return ResponseEntity.status(403)
             .body(ApiResponse.error("관리자 권한이 필요합니다."));
      } catch (IllegalArgumentException e) {
         return ResponseEntity.badRequest()
             .body(ApiResponse.error(e.getMessage()));
      }
   }

   // 모든 문의 조회 (관리자)
   @GetMapping("/admin/tickets")
   public ResponseEntity<ApiResponse<Page<SupportTicketResponseDto>>> getAllTicketsForAdmin(
       @RequestParam(defaultValue = "1") int page,
       @RequestParam(defaultValue = "20") int size,
       HttpServletRequest httpRequest) {

      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         Long adminId = jwtUtil.extractUserId(token);

         Pageable pageable = PageRequest.of(page - 1, size);
         Page<SupportTicketResponseDto> tickets = supportService.getAllTickets(adminId, pageable);

         return ResponseEntity.ok(ApiResponse.success(tickets));
      } catch (AccessDeniedException e) {
         return ResponseEntity.status(403)
             .body(ApiResponse.error("관리자 권한이 필요합니다."));
      }
   }

   // 답변 대기 문의 조회 (관리자)
   @GetMapping("/admin/pending")
   public ResponseEntity<ApiResponse<Page<SupportTicketResponseDto>>> getPendingTickets(
       @RequestParam(defaultValue = "1") int page,
       @RequestParam(defaultValue = "20") int size,
       HttpServletRequest httpRequest) {

      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         Long adminId = jwtUtil.extractUserId(token);

         Pageable pageable = PageRequest.of(page - 1, size);
         Page<SupportTicketResponseDto> tickets = supportService.getPendingTickets(adminId, pageable);

         return ResponseEntity.ok(ApiResponse.success(tickets));
      } catch (AccessDeniedException e) {
         return ResponseEntity.status(403)
             .body(ApiResponse.error("관리자 권한이 필요합니다."));
      }
   }

   // 문의 상세 조회 (관리자)
   @GetMapping("/admin/tickets/{ticketId}")
   public ResponseEntity<ApiResponse<SupportTicketResponseDto>> getTicketDetailForAdmin(
       @PathVariable Long ticketId,
       HttpServletRequest httpRequest) {

      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         Long adminId = jwtUtil.extractUserId(token);

         SupportTicketResponseDto ticket = supportService.getTicketDetail(adminId, ticketId);

         return ResponseEntity.ok(ApiResponse.success(ticket));
      } catch (AccessDeniedException e) {
         return ResponseEntity.status(403)
             .body(ApiResponse.error("관리자 권한이 필요합니다."));
      } catch (IllegalArgumentException e) {
         return ResponseEntity.badRequest()
             .body(ApiResponse.error(e.getMessage()));
      }
   }

   // 문의 상태 변경 (관리자)
   @PatchMapping("/admin/tickets/{ticketId}/status")
   public ResponseEntity<ApiResponse<SupportTicketResponseDto>> updateTicketStatus(
       @PathVariable Long ticketId,
       @RequestParam String status,
       HttpServletRequest httpRequest) {

      try {
         String token = jwtUtil.extractTokenFromRequest(httpRequest);
         Long adminId = jwtUtil.extractUserId(token);

         TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
         SupportTicketResponseDto response = supportService.updateTicketStatus(adminId, ticketId, ticketStatus);

         return ResponseEntity.ok(ApiResponse.success(response));
      } catch (AccessDeniedException e) {
         return ResponseEntity.status(403)
             .body(ApiResponse.error("관리자 권한이 필요합니다."));
      } catch (IllegalArgumentException e) {
         return ResponseEntity.badRequest()
             .body(ApiResponse.error("유효하지 않은 상태입니다: " + status));
      }
   }
}