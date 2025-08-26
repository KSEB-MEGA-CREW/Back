package org.example.mega_crew.domain.history.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.history.dto.WorkTypeStatsDto;
import org.example.mega_crew.domain.history.dto.request.TextTo3DHistoryRequestDto;
import org.example.mega_crew.domain.history.dto.request.TransHistoryRequestDto;
import org.example.mega_crew.domain.history.dto.request.WebcamAnalysisHistoryRequestDto;
import org.example.mega_crew.domain.history.dto.response.TransHistoryResponseDto;
import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.example.mega_crew.domain.history.repository.TranslationHistoryRepository;
import org.example.mega_crew.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationHistoryService {

   private final TranslationHistoryRepository translationHistoryRepository;
   private final UserRepository userRepository;

   // 이미지 → 텍스트 작업 시작
   public TranslationHistory startImageToTextWork(WebcamAnalysisHistoryRequestDto requestDto) {
      TranslationHistory history = TranslationHistory.builder()
          .id(requestDto.getUserId())
          .workType(WorkType.IMAGETOTEXT)
          .inputContent(requestDto.getFileName())
          .inputLength(requestDto.getFileName().length())
          .processingStatus("PROCESSING")
          .userAgent(requestDto.getUserAgent())
          .build();

      return translationHistoryRepository.save(history);
   }


   // 텍스트 → 3D 작업 시작
   public TranslationHistory startTextTo3DWork(TextTo3DHistoryRequestDto requestDto) {
      TranslationHistory history = TranslationHistory.builder()
          .id(requestDto.getUserId())
          .workType(WorkType.TEXTTO3D)
          .inputContent(requestDto.getTextContent())
          .inputLength(requestDto.getTextContent().length())
          .processingStatus("PROCESSING")
          .userAgent(requestDto.getUserAgent())
          .build();

      return translationHistoryRepository.save(history);
   }

   public TranslationHistory updateResult(Long id, String outputContent,
                                          String status, Integer processingTime,
                                          String errorMessage) {
      TranslationHistory history = translationHistoryRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException("History not found"));

      history.setOutputContent(outputContent);
      history.setProcessingStatus(status);
      history.setProcessingTime(processingTime);
      history.setErrorMessage(errorMessage);

      return translationHistoryRepository.save(history);
   }

   // 사용자 히스토리 조회 (활성 기록만)
   public Page<TranslationHistory> getUserHistories(Long userId, int page, int size) {
      Pageable pageable = PageRequest.of(page, size);
      return translationHistoryRepository.findActiveByUserId(userId, pageable);
   }

   // 작업 타입별 히스토리 조회 (활성 기록만)
   public Page<TranslationHistory> getHistoriesByType(Long userId, WorkType workType, int page, int size) {
      Pageable pageable = PageRequest.of(page, size);
      return translationHistoryRepository.findActiveByUserIdAndWorkType(userId, workType, pageable);
   }

   // 통계 조회 (활성 기록만)
   public WorkTypeStatsDto getWorkTypeStats(Long userId, WorkType workType) {
      Long totalCount = translationHistoryRepository.countActiveByUserIdAndWorkType(userId, workType);
      Long successCount = translationHistoryRepository.countActiveByUserIdAndWorkTypeAndProcessingStatus(userId, workType, "SUCCESS");
      Long errorCount = translationHistoryRepository.countActiveByUserIdAndWorkTypeAndProcessingStatus(userId, workType, "ERROR");

      return WorkTypeStatsDto.builder()
          .workType(workType)
          .totalCount(totalCount)
          .successCount(successCount)
          .errorCount(errorCount)
          .build();
   }

   // 만료된 기록 마킹
   @Transactional
   public int markExpiredRecords() {
      int markedCount = translationHistoryRepository.markExpiredRecords(LocalDateTime.now());
      log.info("Marked {} records as expired", markedCount);
      return markedCount;
   }

   // 오래된 만료 기록 삭제
   @Transactional
   public int deleteOldExpiredRecords(int daysAfterExpiration) {
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAfterExpiration);
      int deletedCount = translationHistoryRepository.deleteExpiredRecords(cutoffDate);
      log.info("Deleted {} old expired records", deletedCount);
      return deletedCount;
   }

   // 사용자별 기록 수 제한
   @Transactional
   public void limitUserRecords(Long userId, int maxRecords) {
      List<TranslationHistory> userRecords = translationHistoryRepository.findByUserIdOrderByCreatedDateDesc(userId);

      if (userRecords.size() > maxRecords) {
         List<TranslationHistory> recordsToExpire = userRecords.subList(maxRecords, userRecords.size());
         recordsToExpire.forEach(TranslationHistory::markAsExpired);
         translationHistoryRepository.saveAll(recordsToExpire);
         log.info("Marked {} old records as expired for user {}", recordsToExpire.size(), userId);
      }
   }

   // 스케줄러: 매일 자정에 만료된 기록 정리
   @Scheduled(cron = "0 0 0 * * *")
   @Transactional
   public void cleanupExpiredRecords() {
      log.info("Starting cleanup of expired records");
      int markedCount = markExpiredRecords();
      int deletedCount = deleteOldExpiredRecords(7);
      log.info("Cleanup completed: {} marked, {} deleted", markedCount, deletedCount);
   }

   // 스케줄러: 사용자별 기록 수 제한 (주간)
   @Scheduled(cron = "0 0 2 * * SUN")
   @Transactional
   public void limitAllUserRecords() {
      log.info("Starting user record limitation");

      List<Long> userIds = translationHistoryRepository.findAll()
          .stream()
          .map(history -> history.getUser().getId())
          .distinct()
          .toList();

      userIds.forEach(userId -> limitUserRecords(userId, 100));
      log.info("User record limitation completed for {} users", userIds.size());
   }

   @Transactional
   public TransHistoryResponseDto submitTranslationFeedback(TransHistoryRequestDto requestDto) {
      try {
         log.info("Translation feedback received - historyId: {}, feedback: {}, translatedText: {}, translatedTime: {}",
             requestDto.getHistoryId(),
             requestDto.getFeedback(),
             requestDto.getTranslatedText(),
             requestDto.getTranslatedTime());

         // 히스토리 조회
         TranslationHistory history = translationHistoryRepository.findById(requestDto.getHistoryId())
             .orElseThrow(() -> new EntityNotFoundException("Translation history not found with id: " + requestDto.getHistoryId()));

         // 요청에 userId가 있다면 권한 확인 (선택적)
         if (requestDto.getUserId() != null && !history.getUser().getId().equals(requestDto.getUserId())) {
            log.warn("User {} tried to submit feedback for history {} owned by user {}",
                requestDto.getUserId(), requestDto.getHistoryId(), history.getUser().getId());
            return TransHistoryResponseDto.builder()
                .historyId(requestDto.getHistoryId())
                .feedback(requestDto.getFeedback())
                .translatedText(requestDto.getTranslatedText())
                .translatedTime(requestDto.getTranslatedTime())
                .status("ERROR")
                .message("Unauthorized: Cannot submit feedback for other user's translation history")
                .build();
         }

         // 이미 평가가 제출된 경우 확인
         if (history.hasFeedback()) {
            log.warn("Feedback already exists for history ID: {}", requestDto.getHistoryId());
            return TransHistoryResponseDto.builder()
                .historyId(requestDto.getHistoryId())
                .feedback(requestDto.getFeedback())
                .translatedText(requestDto.getTranslatedText())
                .translatedTime(requestDto.getTranslatedTime())
                .status("WARNING")
                .message("Feedback already exists for this translation history")
                .build();
         }

         // 평가 정보 업데이트
         history.updateFeedback(
             requestDto.getFeedback(),
             requestDto.getTranslatedText(),
             requestDto.getTranslatedTime()
         );

         // 저장
         TranslationHistory updatedHistory = translationHistoryRepository.save(history);

         // 성공 응답 생성
         return TransHistoryResponseDto.builder()
             .historyId(updatedHistory.getId())
             .feedback(updatedHistory.getFeedback())
             .translatedText(updatedHistory.getTranslatedText())
             .translatedTime(updatedHistory.getTranslatedTime())
             .status("SUCCESS")
             .message("Translation feedback submitted successfully")
             .build();

      } catch (EntityNotFoundException e) {
         log.error("Translation history not found: {}", e.getMessage());
         return TransHistoryResponseDto.builder()
             .historyId(requestDto.getHistoryId())
             .feedback(requestDto.getFeedback())
             .translatedText(requestDto.getTranslatedText())
             .translatedTime(requestDto.getTranslatedTime())
             .status("ERROR")
             .message("Translation history not found")
             .build();
      } catch (Exception e) {
         log.error("Error processing translation feedback: {}", e.getMessage(), e);
         return TransHistoryResponseDto.builder()
             .historyId(requestDto.getHistoryId())
             .feedback(requestDto.getFeedback())
             .translatedText(requestDto.getTranslatedText())
             .translatedTime(requestDto.getTranslatedTime())
             .status("ERROR")
             .message("Failed to process feedback: " + e.getMessage())
             .build();
      }
   }
}