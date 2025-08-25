package org.example.mega_crew.domain.history.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.history.Service.TranslationHistoryService;
import org.example.mega_crew.domain.history.dto.WorkTypeStatsDto;
import org.example.mega_crew.domain.history.dto.request.TextTo3DHistoryRequestDto;
import org.example.mega_crew.domain.history.dto.request.WebcamAnalysisHistoryRequestDto;
import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/translation-histories")
@RestController
public class TranslationHistoryController {

   private final TranslationHistoryService translationHistoryService;

   // 이미지 → 텍스트 작업 시작
   @PostMapping("/image-to-text")
   public ResponseEntity<TranslationHistory> startImageToText(
       @Valid @RequestBody WebcamAnalysisHistoryRequestDto requestDto,
       HttpServletRequest request) {

      requestDto.setUserAgent(request.getHeader("User-Agent"));

      TranslationHistory history = translationHistoryService.startImageToTextWork(requestDto);
      return ResponseEntity.ok(history);
   }

   // 텍스트 → 3D 작업 시작
   @PostMapping("/text-to-3d")
   public ResponseEntity<TranslationHistory> startTextTo3D(
       @Valid @RequestBody TextTo3DHistoryRequestDto requestDto,
       HttpServletRequest request) {

      requestDto.setUserAgent(request.getHeader("User-Agent"));

      TranslationHistory history = translationHistoryService.startTextTo3DWork(requestDto);
      return ResponseEntity.ok(history);
   }

   @PutMapping("/{id}/result")
   public ResponseEntity<TranslationHistory> updateResult(
       @PathVariable Long id,
       @RequestBody Map<String, Object> result) {

      TranslationHistory updated = translationHistoryService.updateResult(
          id,
          (String) result.get("outputContent"),
          (String) result.get("status"),
          (Integer) result.get("processingTime"),
          (String) result.get("errorMessage")
      );

      return ResponseEntity.ok(updated);
   }

   @GetMapping("/user/{userId}")
   public ResponseEntity<Page<TranslationHistory>> getUserHistories(
       @PathVariable Long userId,
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size) {

      Page<TranslationHistory> histories = translationHistoryService.getUserHistories(userId, page, size);
      return ResponseEntity.ok(histories);
   }

   @GetMapping("/user/{userId}/type/{workType}")
   public ResponseEntity<Page<TranslationHistory>> getHistoriesByType(
       @PathVariable Long userId,
       @PathVariable WorkType workType,
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size) {

      Page<TranslationHistory> histories = translationHistoryService.getHistoriesByType(userId, workType, page, size);
      return ResponseEntity.ok(histories);
   }

   @GetMapping("/user/{userId}/stats/{workType}")
   public ResponseEntity<WorkTypeStatsDto> getWorkTypeStats(
       @PathVariable Long userId,
       @PathVariable WorkType workType) {

      WorkTypeStatsDto stats = translationHistoryService.getWorkTypeStats(userId, workType);
      return ResponseEntity.ok(stats);
   }

   // 관리자용 수동 정리 API
   @PostMapping("/admin/cleanup")
   public ResponseEntity<Map<String, Integer>> manualCleanup() {
      int marked = translationHistoryService.markExpiredRecords();
      int deleted = translationHistoryService.deleteOldExpiredRecords(7);

      Map<String, Integer> result = Map.of(
          "markedAsExpired", marked,
          "deleted", deleted
      );

      return ResponseEntity.ok(result);
   }
}
