package org.example.mega_crew.domain.webcam.repository;

import org.example.mega_crew.domain.webcam.entity.WebcamData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebcamDataRepository extends JpaRepository<WebcamData, Long> {

    @Query("SELECT wd FROM WebcamData wd WHERE wd.session.sessionId = :sessionId ORDER BY wd.frameNumber")
    List<WebcamData> findBySessionIdOrderByFrameNumber(String sessionId);

    Long countBySessionSessionId(String sessionId);
}
