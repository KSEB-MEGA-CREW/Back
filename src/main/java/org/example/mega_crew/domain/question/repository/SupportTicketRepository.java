package org.example.mega_crew.domain.question.repository;

import io.lettuce.core.dynamic.annotation.Param;
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

   // 공개 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.isPublic = true ORDER BY s.createdDate DESC")
   Page<SupportTicket> findPublicTickets(Pageable pageable);

   // 카테고리별 공개 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.category = :category AND s.isPublic = true ORDER BY s.createdDate DESC")
   Page<SupportTicket> findPublicTicketsByCategory(@Param("category") TicketCategory category, Pageable pageable);

   // 상태별 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.status = :status ORDER BY s.createdDate DESC")
   Page<SupportTicket> findTicketsByStatus(@Param("status") TicketStatus status, Pageable pageable);

   // 관리자용 쿼리들 추가
   @Query("SELECT s FROM SupportTicket s ORDER BY s.createdDate DESC")
   Page<SupportTicket> findAllTicketsForAdmin(Pageable pageable);

   @Query("SELECT s FROM SupportTicket s WHERE s.status IN ('PENDING', 'IN_PROGRESS') ORDER BY s.createdDate ASC")
   Page<SupportTicket> findPendingTickets(Pageable pageable);

   // 특정 티켓 조회 (관리자용)
   @Query("SELECT s FROM SupportTicket s WHERE s.id = :ticketId")
   Optional<SupportTicket> findTicketById(@Param("ticketId") Long ticketId);
}
