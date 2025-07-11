package org.example.mega_crew.domain.webcam.service;


import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.example.mega_crew.domain.webcam.dto.request.WebcamDataRequest;
import org.example.mega_crew.domain.webcam.dto.response.WebcamSessionResponse;
import org.example.mega_crew.domain.webcam.entity.WebcamSession;
import org.example.mega_crew.domain.webcam.repository.WebcamSessionRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebcamService {

    private final WebcamSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final LocalStorageService storageService;

    public WebcamSessionResponse startSession(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        WebcamSession session = WebcamSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(user)
                .build();

        sessionRepository.save(session);
        log.info("Session started: {}", session.getSessionId());

        return WebcamSessionResponse.builder()
                .sessionId(session.getSessionId())
                .status("STARTED")
                .build();
    }

    public void processFrame(String sessionId, WebcamDataRequest request) {
        WebcamSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (session.getStatus() != WebcamSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("세션이 활성 상태가 아닙니다");
        }

        storageService.saveFrame(sessionId, request.getFrameNumber(), request.getFrameData());
    }

    public WebcamSessionResponse endSession(String sessionId, byte[] videoData) {
        WebcamSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (videoData != null && videoData.length > 0) {
            String videoPath = storageService.saveVideo(sessionId, videoData);
            session.setVideoPath(videoPath);
        }

        session.setStatus(WebcamSession.SessionStatus.COMPLETED);
        sessionRepository.save(session);

        log.info("Session ended: {}", sessionId);

        return WebcamSessionResponse.builder()
                .sessionId(sessionId)
                .status("COMPLETED")
                .videoUrl(session.getVideoPath())
                .build();
    }
}
