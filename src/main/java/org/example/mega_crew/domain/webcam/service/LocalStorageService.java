package org.example.mega_crew.domain.webcam.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@Service
public class LocalStorageService {


    private final String storagePath = "./uploads/webcam";

    public void saveFrame(String sessionId, Long frameNumber, String base64Data) {
        try {
            // 디렉토리 생성
            Path sessionDir = Paths.get(storagePath, sessionId, "frames");
            Files.createDirectories(sessionDir);

            // Base64 디코드
            byte[] frameData = Base64.getDecoder().decode(base64Data);

            // 파일 저장
            Path framePath = sessionDir.resolve(String.format("frame_%06d.jpg", frameNumber));
            Files.write(framePath, frameData);

            log.debug("Frame saved: {}", framePath);
        } catch (IOException e) {
            log.error("Failed to save frame", e);
            throw new RuntimeException("프레임 저장 실패", e);
        }
    }

    public String saveVideo(String sessionId, byte[] videoData) {
        try {
            // 디렉토리 생성
            Path sessionDir = Paths.get(storagePath, sessionId);
            Files.createDirectories(sessionDir);

            // 비디오 저장
            Path videoPath = sessionDir.resolve("video.webm");
            Files.write(videoPath, videoData);

            log.info("Video saved: {}", videoPath);
            return videoPath.toString();
        } catch (IOException e) {
            log.error("Failed to save video", e);
            throw new RuntimeException("비디오 저장 실패", e);
        }
    }
}
