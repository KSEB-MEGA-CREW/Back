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

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

   // 사용자별 문의 조회
   @Query("SELECT s FROM SupportTicket s WHERE s.userId = :userId ORDER BY s.createdDate DESC")
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
}
