package org.example.mega_crew.domain.history.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.mega_crew.domain.history.dto.WorkTypeStatsDto;
import org.example.mega_crew.domain.history.dto.request.TextTo3DHistoryRequestDto;
import org.example.mega_crew.domain.history.dto.request.WebcamAnalysisHistoryRequestDto;
import org.example.mega_crew.domain.history.entity.TranslationHistory;
import org.example.mega_crew.domain.history.entity.WorkType;
import org.example.mega_crew.domain.history.repository.TranslationHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslationHistoryService {

   private final TranslationHistoryRepository translationHistoryRepository;

   // 이미지 → 텍스트 작업 시작
   public TranslationHistory startImageToTextWork(WebcamAnalysisHistoryRequestDto requestDto) {
      TranslationHistory history = TranslationHistory.builder()
          .id(requestDto.getUserId())
          .workType(WorkType.IMAGETOTEXT)
          .inputContent(requestDto.getFileName())
          .inputLength(requestDto.getFileName().length())
          .processingStatus("PROCESSING")
          .userAgent(requestDto.getUserAgent())
          .clientIp(requestDto.getClientIp())
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
          .clientIp(requestDto.getClientIp())
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

   public Page<TranslationHistory> getUserHistories(Long userId, int page, int size) {
      Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
      return translationHistoryRepository.findByUserId(userId, (java.awt.print.Pageable) pageable);
   }

   public List<TranslationHistory> getHistoriesByType(Long userId, WorkType workType) {
      return translationHistoryRepository.findByUserIdAndWorkType(userId, workType);
   }

   public Page<TranslationHistory> getHistoriesByType(Long userId, WorkType workType, int page, int size) {
      Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
      return translationHistoryRepository.findByUserIdAndWorkType(userId, workType, (java.awt.print.Pageable) pageable);
   }

   public WorkTypeStatsDto getWorkTypeStats(Long userId, WorkType workType) {
      Long totalCount = translationHistoryRepository.countByUserIdAndWorkType(userId, workType);
      // 추가 통계 로직...
      return WorkTypeStatsDto.builder()
          .workType(workType)
          .totalCount(totalCount)
          .build();
   }
}
