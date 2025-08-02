package org.example.mega_crew.domain.quiz.repository;

import org.example.mega_crew.domain.quiz.entity.QuizCategoryRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizCategoryRecordRepository extends JpaRepository<QuizCategoryRecords, Long> {

   // 사용자별 카테고리별 통계
   @Query("SELECT qcr.category, SUM(qcr.correctCount) FROM QuizCategoryRecords qcr " +
       "WHERE qcr.user.id = :userId GROUP BY qcr.category")
   List<Object[]> getCategoryStatsByUser(@Param("userId") Long userId);
}
