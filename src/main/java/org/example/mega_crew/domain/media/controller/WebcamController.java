package org.example.mega_crew.domain.media.controller;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.media.dto.request.WebcamAnalysisRequest;
import org.example.mega_crew.domain.media.dto.response.WebcamAnalysisResponse;
import org.example.mega_crew.domain.media.service.ImageAnalysisService;
import org.example.mega_crew.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webcam")
@RequiredArgsConstructor
@Tag(name = "웹캠 이미지 분석", description = "웹캐 이미지 분석 API")
public class WebcamController {
    private final ImageAnalysisService imageAnalysisService;

    // 히스토리 기록이 포함된 새로운 엔드포인트
    @PostMapping("/analyze-with-history")
    @Operation(summary = "웹캠 이미지 분석 (히스토리 기록)", description = "웹캠으로 캡쳐한 이미지를 AI 서버로 전송하여 번역된 텍스트를 받고 히스토리에 기록합니다.")
    public ResponseEntity<ApiResponse<WebcamAnalysisResponse>> analyzeImageWithHistory(
        @Valid @ModelAttribute WebcamAnalysisRequest request,
        @RequestParam("userId") Long userId,
        HttpServletRequest httpRequest) {

        String userAgent = httpRequest.getHeader("User-Agent");

        WebcamAnalysisResponse response = imageAnalysisService.analyzeImage(request, userId, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기존 엔드포인트 (하위 호환성 유지)
    @PostMapping("/analyze")
    @Operation(summary = "웹캠 이미지 분석", description = "웹캠으로 캡쳐한 이미지를 AI 서버로 전송하여 번역된 텍스트를 받습니다.")
    public ResponseEntity<ApiResponse<WebcamAnalysisResponse>> analyzeImage(
        @Valid @ModelAttribute WebcamAnalysisRequest request) {

        WebcamAnalysisResponse response = imageAnalysisService.analyzeImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
