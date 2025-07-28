package org.example.mega_crew.domain.signlanguageFrame.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrameRequest { // frontend로부터 받는 프레임의 dto
    @NotBlank(message = "프레임 데이터는 필수입니다.")
    private String frameData; // Base64 encoded JPEG image -> 인식 정확도와 성능을 고려한 최적의 압축 설정
    // MVP에선 배치 처리를 적용하지 않고 개별 전송으로 우선 구현한다
    // 추후 사용자 경험을 토대로 batch 최적화를 선택한 후 비교하여 선택한다
    // A/B test 진행

    @NotNull
    private Long userId; // JWT에서 추출하여 저장

    @NotNull(message = "타임 스탬프는 필수입니다.")
    private Long timestamp;

    @NotBlank(message = "세션 ID는 필수입니다.")
    private String sessionId; // frame 순서 및 연속성 보장을 위해 필요

    @NotNull(message = "프레임 인덱스는 필수입니다.")
    private Integer frameIndex;

    // frame 관련 메타 데이터 -> 필요하면 추가하기
    // MVP에선 제외
}
