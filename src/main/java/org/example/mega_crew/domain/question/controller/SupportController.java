package org.example.mega_crew.domain.question.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.question.dto.request.SupportTicketRequestDto;
import org.example.mega_crew.domain.question.dto.response.SupportTicketResponseDto;
import org.example.mega_crew.domain.question.service.SupportService;
import org.example.mega_crew.global.common.ApiResponse;
import org.example.mega_crew.global.security.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size,
       HttpServletRequest httpRequest) {

      String token = jwtUtil.extractTokenFromRequest(httpRequest);
      Long userId = jwtUtil.extractUserId(token);

      Pageable pageable = PageRequest.of(page, size);
      Page<SupportTicketResponseDto> tickets = supportService.getMyTickets(userId, pageable);

      return ResponseEntity.ok(ApiResponse.success(tickets));
   }

   // 공개 문의 게시판 조회
   @GetMapping("/public")
   public ResponseEntity<ApiResponse<Page<SupportTicketResponseDto>>> getPublicTickets(
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size,
       @RequestParam(required = false) String category) {

      Pageable pageable = PageRequest.of(page, size);
      Page<SupportTicketResponseDto> tickets;

      if (category != null && !category.trim().isEmpty()) {
         tickets = supportService.getPublicTicketsByCategory(category, pageable);
      } else {
         tickets = supportService.getPublicTickets(pageable);
      }

      return ResponseEntity.ok(ApiResponse.success(tickets));
   }
}
