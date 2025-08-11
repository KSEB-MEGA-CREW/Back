package org.example.mega_crew.domain.history.dto;

import lombok.*;
import org.example.mega_crew.domain.history.entity.WorkType;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
public class WorkTypeStatsDto {

   private WorkType workType;

   private Long totalCount;
   private Long successCount;
   private Long errorCount;
}
