package org.example.mega_crew.domain.webcam.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

@Entity
@Table(name = "webcam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebcamSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "video_path")
    private String videoPath;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.ACTIVE;

    public enum SessionStatus {
        ACTIVE, COMPLETED
    }
}
