package org.example.mega_crew.domain.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.media.dto.request.TextTo3DRequest;
import org.example.mega_crew.domain.media.dto.response.TextTo3DResponse;
import org.example.mega_crew.domain.media.service.TextTo3DService;
import org.example.mega_crew.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
@Tag(name = "텍스트를 번역하여 수어 3D 생성", description = "텍스트를 번역해 3D 모델로 변환하는 API")
public class TextTo3DController {
    private final TextTo3DService textTo3DService;

    @PostMapping("/generate")
    @Operation(summary = "텍스트 번역하여 3D 생성", description = "입력된 텍스트를 AI 서버로 전송하여 3D 모델을 생성합니다.")
    public ResponseEntity<ApiResponse<TextTo3DResponse>> generate3D(
            @Valid @RequestBody TextTo3DRequest request
    ){
        TextTo3DResponse response = textTo3DService.generate3D(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
