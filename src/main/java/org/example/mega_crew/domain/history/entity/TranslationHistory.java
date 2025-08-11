package org.example.mega_crew.domain.history.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mega_crew.domain.user.entity.User;
import org.example.mega_crew.global.common.BaseEntity;

@Entity
@Table(name = "translation_histories")
@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationHistory extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", referencedColumnName = "id")
   private User user;

   @Enumerated(EnumType.STRING)
   @Column(name="work_type", nullable = false)
   private WorkType workType;

   @Column(name="input_content")
   private String inputContent;

   // 히스토리 업데이트를 위한 편의 메서드들
   @Column(name="output_content")
   private String outputContent;

   @Column(name = "processing_status")
   private String processingStatus;

   @Column(name = "processing_time")
   private Integer processingTime;

   @Column(name = "error_message")
   private String errorMessage;

   @Column(name = "user_agent")
   private String userAgent;

   @Column(name = "client_ip")
   private String clientIp;

   @Column(name = "input_length")
   private Integer inputLength;

}
