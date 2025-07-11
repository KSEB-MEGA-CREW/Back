package org.example.mega_crew.domain.webcam.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.global.common.BaseEntity;

@Entity
@Table(name = "webcam_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebcamData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WebcamSession session;

    @Column(name = "frame_number", nullable = false)
    private Long frameNumber;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "frame_size")
    private Long frameSize;
    // 추후 웹캠데이터를 s3에 저장해야 할 경우, 데이터 멤버로 s3Key 추가하기
}
