package org.example.mega_crew.domain.webcam.repository;

import org.example.mega_crew.domain.webcam.entity.WebcamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebcamSessionRepository extends JpaRepository<WebcamSession, Long> {
    Optional<WebcamSession> findBySessionId(String sessionId);
}