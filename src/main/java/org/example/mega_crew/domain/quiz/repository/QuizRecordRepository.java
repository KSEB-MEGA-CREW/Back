package org.example.mega_crew.domain.quiz.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.mega_crew.domain.quiz.entity.QuizRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuizRecordRepository extends JpaRepository<QuizRecords, Long> {

   // 특정 월의 사용자 일별 퀴즈 정답률 조회
   @Query("SELECT DATE(qr.createdDate) as date, " +
       "SUM(qr.correctCount) as totalCorrect, " +
       "COUNT(qr) * 5 as totalQuestions " +
       "FROM QuizRecords qr " +
       "WHERE DATE(qr.createdDate) BETWEEN :startDate AND :endDate " +
       "AND qr.user.id = :userId " +
       "GROUP BY DATE(qr.createdDate)")
   List<Object[]> getMonthlyQuizStatsByUser(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("userId") Long userId);

   @Modifying
   @Query("DELETE FROM QuizRecords qr WHERE qr.user.id = :userId")
   void deleteByUserId(@Param("userId") Long userId);
}
