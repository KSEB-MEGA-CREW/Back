package org.example.mega_crew.domain.question.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.example.mega_crew.domain.question.entity.SupportTicket;
import org.example.mega_crew.domain.question.entity.TicketCategory;
import org.example.mega_crew.domain.question.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

   // 사용자별 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.user.id = :userId ORDER BY s.createdDate DESC")
   Page<SupportTicket> findTicketsByUserId(@Param("userId") Long userId, Pageable pageable);

   // 게시판용 - 모든 문의 조회 (공개 + 비공개) - Service에서 호출하는 메서드명
   @Query("SELECT s FROM SupportTicket s ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsForBoard(Pageable pageable);

   // 게시판용 - 카테고리별 모든 문의 조회 (공개 + 비공개) - Service에서 호출하는 메서드명
   @Query("SELECT s FROM SupportTicket s WHERE s.category = :category ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsByCategoryForBoard(@Param("category") TicketCategory category, Pageable pageable);

   // 모든 문의 조회 (공개 + 비공개) - 기존 메서드
   @Query("SELECT s FROM SupportTicket s ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsForPublicBoard(Pageable pageable);

   // 카테고리별 모든 문의 조회 (공개 + 비공개) - 기존 메서드
   @Query("SELECT s FROM SupportTicket s WHERE s.category = :category ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsByCategory(@Param("category") TicketCategory category, Pageable pageable);

   // 공개 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.isPublic = true ORDER BY s.createdDate DESC")
   Page<SupportTicket> findPublicTickets(Pageable pageable);

   // 카테고리별 공개 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.category = :category AND s.isPublic = true ORDER BY s.createdDate DESC")
   Page<SupportTicket> findPublicTicketsByCategory(@Param("category") TicketCategory category, Pageable pageable);

   // 상태별 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.status = :status ORDER BY s.createdDate DESC")
   Page<SupportTicket> findTicketsByStatus(@Param("status") TicketStatus status, Pageable pageable);

   // 내 문의 세부 내용 조회
   Optional<SupportTicket> findByIdAndUserId(Long id, Long userId);

   // 관리자용 쿼리들 추가
   @Query("SELECT s FROM SupportTicket s ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsForAdmin(Pageable pageable);

   @Query("SELECT s FROM SupportTicket s WHERE s.status IN ('PENDING', 'IN_PROGRESS') ORDER BY s.createdDate ASC")
   Page<SupportTicket> findPendingTickets(Pageable pageable);

   // 특정 티켓 조회 (관리자용)
   @Query("SELECT s FROM SupportTicket s WHERE s.id = :ticketId")
   Optional<SupportTicket> findTicketById(@Param("ticketId") Long ticketId);

   @Modifying
   @Query("DELETE FROM SupportTicket st WHERE st.user.id = :userId")
   void deleteByUserId(@Param("userId") Long userId);
}