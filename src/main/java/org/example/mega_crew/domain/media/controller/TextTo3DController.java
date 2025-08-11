package org.example.mega_crew.domain.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.media.dto.request.TextTo3DRequest;
import org.example.mega_crew.domain.media.dto.response.TextTo3DResponse;
import org.example.mega_crew.domain.media.service.TextTo3DService;
import org.example.mega_crew.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
@Tag(name = "텍스트를 번역하여 수어 3D 생성", description = "텍스트를 번역해 3D 모델로 변환하는 API")
public class TextTo3DController {
    private final TextTo3DService textTo3DService;

    // 히스토리 기록이 포함된 새로운 엔드포인트
    @PostMapping("/generate-with-history")
    @Operation(summary = "텍스트 번역하여 3D 생성 (히스토리 기록)", description = "입력된 텍스트를 AI 서버로 전송하여 3D 모델을 생성하고 히스토리에 기록합니다.")
    public ResponseEntity<ApiResponse<TextTo3DResponse>> generate3DWithHistory(
        @Valid @RequestBody TextTo3DRequest request,
        @RequestParam("userId") Long userId,
        HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        TextTo3DResponse response = textTo3DService.generate3D(request, userId, userAgent, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기존 엔드포인트 (하위 호환성 유지)
    @PostMapping("/generate")
    @Operation(summary = "텍스트 번역하여 3D 생성", description = "입력된 텍스트를 AI 서버로 전송하여 3D 모델을 생성합니다.")
    public ResponseEntity<ApiResponse<TextTo3DResponse>> generate3D(
        @Valid @RequestBody TextTo3DRequest request) {

        TextTo3DResponse response = textTo3DService.generate3D(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
