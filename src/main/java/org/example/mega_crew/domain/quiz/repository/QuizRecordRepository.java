package org.example.mega_crew.domain.quiz.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.mega_crew.domain.quiz.entity.QuizRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuizRecordRepository extends JpaRepository<QuizRecords, Long> {

   // 특정 날짜의 특정 사용자 최고 정답 개수 조회
   @Query("SELECT COALESCE(MAX(qr.correctCount), 0) FROM QuizRecords qr WHERE DATE(qr.createdDate) = :date AND qr.user.id = :userId")
   Integer getMaxCorrectCountByDateAndUser(@Param("date") LocalDate date, @Param("userId") Long userId);
}
