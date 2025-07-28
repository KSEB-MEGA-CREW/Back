package org.example.mega_crew.domain.quiz.repository;

import org.example.mega_crew.domain.quiz.entity.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRecordRepository extends JpaRepository<QuizRecord, Long> {

}
