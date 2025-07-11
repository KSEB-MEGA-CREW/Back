package org.example.mega_crew.domain.webcam.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.webcam.dto.request.WebcamDataRequest;
import org.example.mega_crew.domain.webcam.dto.response.WebcamSessionResponse;
import org.example.mega_crew.domain.webcam.service.WebcamService;
import org.example.mega_crew.global.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/webcam")
@RequiredArgsConstructor
@Tag(name = "Webcam API", description = "웹캠 데이터 로컬 저장 API")
@SecurityRequirement(name = "bearerAuth")
public class WebcamController {

    private final WebcamService webcamService;

    @PostMapping("/start")
    public ApiResponse<WebcamSessionResponse> startSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        WebcamSessionResponse response = webcamService.startSession(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @PostMapping("/frame")
    public ApiResponse<Void> sendFrame(@RequestBody WebcamDataRequest request) {
        webcamService.processFrame(request.getSessionID(), request);
        return ApiResponse.success(null);
    }

    @PostMapping(value = "/end", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WebcamSessionResponse> endSession(
            @RequestParam String sessionId,
            @RequestPart(value = "video", required = false) MultipartFile videoFile) {
        try {
            byte[] videoData = videoFile != null ? videoFile.getBytes() : null;
            WebcamSessionResponse response = webcamService.endSession(sessionId, videoData);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error ending session", e);
            return ApiResponse.error("세션 종료 실패");
        }
    }
}
