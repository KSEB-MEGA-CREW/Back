package org.example.mega_crew.domain.history.repository;

import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TranslationHistoryRepository extends JpaRepository<TranslationHistory, Long> {

   // 활성 기록만 조회 (만료되지 않은 것만)
   @Query("SELECT t FROM TranslationHistory t WHERE t.user.id = :userId AND t.isExpired = false ORDER BY t.createdDate DESC")
   Page<TranslationHistory> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

   @Query("SELECT t FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType AND t.isExpired = false ORDER BY t.createdDate DESC")
   Page<TranslationHistory> findActiveByUserIdAndWorkType(@Param("userId") Long userId, @Param("workType") WorkType workType, Pageable pageable);

   // 통계 쿼리들 (활성 데이터만)
   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType AND t.isExpired = false")
   Long countActiveByUserIdAndWorkType(@Param("userId") Long userId, @Param("workType") WorkType workType);

   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType AND t.processingStatus = :status AND t.isExpired = false")
   Long countActiveByUserIdAndWorkTypeAndProcessingStatus(@Param("userId") Long userId, @Param("workType") WorkType workType, @Param("status") String status);

   @Modifying
   @Query("DELETE FROM TranslationHistory th WHERE th.user.id = :userId")
   void deleteByUserId(@Param("userId") Long userId);

   // 만료 관리 쿼리들
   @Modifying
   @Query("UPDATE TranslationHistory t SET t.isExpired = true WHERE t.expiresAt < :now AND t.isExpired = false")
   int markExpiredRecords(@Param("now") LocalDateTime now);

   @Modifying
   @Query("DELETE FROM TranslationHistory t WHERE t.isExpired = true AND t.expiresAt < :cutoffDate")
   int deleteExpiredRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

   @Query("SELECT t FROM TranslationHistory t WHERE t.user.id = :userId ORDER BY t.createdDate DESC")
   List<TranslationHistory> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);

   // 평가가 있는/없는 히스토리 조회
   @Query("SELECT t FROM TranslationHistory t WHERE t.user.id = :userId AND t.feedback IS NOT NULL AND t.isExpired = false ORDER BY t.feedbackSubmittedAt DESC")
   Page<TranslationHistory> findActiveFeedbackByUserId(@Param("userId") Long userId, Pageable pageable);

   @Query("SELECT t FROM TranslationHistory t WHERE t.user.id = :userId AND t.feedback IS NULL AND t.isExpired = false ORDER BY t.createdDate DESC")
   Page<TranslationHistory> findActiveWithoutFeedbackByUserId(@Param("userId") Long userId, Pageable pageable);

   // 평가별 통계
   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.feedback = :feedback AND t.isExpired = false")
   Long countByUserIdAndFeedback(@Param("userId") Long userId, @Param("feedback") String feedback);

   // 특정 작업 타입의 평가별 통계
   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType AND t.feedback = :feedback AND t.isExpired = false")
   Long countByUserIdAndWorkTypeAndFeedback(@Param("userId") Long userId, @Param("workType") WorkType workType, @Param("feedback") String feedback);
}

