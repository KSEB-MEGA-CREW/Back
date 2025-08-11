package org.example.mega_crew.domain.history.repository;

import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationHistoryRepository extends JpaRepository<TranslationHistory, Long> {

   List<TranslationHistory> findByUserId(Long userId);
   List<TranslationHistory> findByUserIdAndWorkType(Long userId, WorkType workType);
   List<TranslationHistory> findByProcessingStatus(String status);
   Page<TranslationHistory> findByUserId(Long userId, Pageable pageable);
   Page<TranslationHistory> findByUserIdAndWorkType(Long userId, WorkType workType, Pageable pageable);

   // 통계 쿼리들
   Long countByUserId(Long userId);
   Long countByUserIdAndWorkType(Long userId, WorkType workType);
   Long countByUserIdAndWorkTypeAndProcessingStatus(Long userId, WorkType workType, String status);

   // 커스텀 쿼리 (User 엔티티 관계 고려)
   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType")
   Long countUserWorksByType(@Param("userId") Long userId, @Param("workType") WorkType workType);

   @Query("SELECT COUNT(t) FROM TranslationHistory t WHERE t.user.id = :userId AND t.workType = :workType AND t.processingStatus = :status")
   Long countUserWorksByTypeAndStatus(@Param("userId") Long userId, @Param("workType") WorkType workType, @Param("status") String status);
}
