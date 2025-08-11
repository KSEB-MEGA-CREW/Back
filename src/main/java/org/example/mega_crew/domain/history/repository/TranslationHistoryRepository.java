package org.example.mega_crew.domain.history.repository;

import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface TranslationHistoryRepository extends JpaRepository<TranslationHistory, Long> {
   List<TranslationHistory> findByUserId(Long userId);
   List<TranslationHistory> findByUserIdAndWorkType(Long userId, WorkType workType);
   List<TranslationHistory> findByProcessingStatus(String status);
   Page<TranslationHistory> findByUserId(Long userId, Pageable pageable);
   Page<TranslationHistory> findByUserIdAndWorkType(Long userId, WorkType workType, Pageable pageable);

   Long countByUserIdAndWorkType(Long userId, WorkType workType);
}
