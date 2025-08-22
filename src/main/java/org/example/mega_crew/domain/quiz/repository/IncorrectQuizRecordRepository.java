package org.example.mega_crew.domain.quiz.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.mega_crew.domain.quiz.entity.IncorrectQuizRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncorrectQuizRecordRepository extends JpaRepository<IncorrectQuizRecords, Long> {

   // 사용자별 오답 조회
   @Query("SELECT qir FROM IncorrectQuizRecords qir WHERE qir.user.id = :userId ORDER BY qir.createdDate DESC")
   List<IncorrectQuizRecords> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);

   // 사용자별 카테고리별 오답 조회
   @Query("SELECT qir FROM IncorrectQuizRecords qir WHERE qir.user.id = :userId AND qir.category = :category ORDER BY qir.createdDate DESC")
   List<IncorrectQuizRecords> findByUserIdAndCategoryOrderByCreatedDateDesc(@Param("userId") Long userId, @Param("category") String category);

   // 특정 단어의 오답 횟수 조회
   @Query("SELECT COUNT(qir) FROM IncorrectQuizRecords qir WHERE qir.user.id = :userId AND qir.word = :word")
   Long countByUserIdAndWord(@Param("userId") Long userId, @Param("word") String word);
}
