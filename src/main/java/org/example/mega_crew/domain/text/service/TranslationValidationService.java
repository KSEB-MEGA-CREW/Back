package org.example.mega_crew.domain.text.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.text.dto.TextTranslationRequest;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationValidationService {

    private static final int MAX_TEXT_LENGTH = 200;

    // 현재 한국어 입력만 처리 가능
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^(ko)$");

    public void validateTranslationRequest(TextTranslationRequest request) {
        log.debug("번역 요청 검증 시작: sessionId={}, textLength={}",
                request.getSessionId(), request.getText().length());
        validateText(request.getText());
        validateTimestamp(request.getTimestamp());
        validateSessionId(request.getSessionId());
        validateUserId(request.getUserId());
        validateLanguage(request.getLanguage());

        log.debug("번역 요청 검증 완료: sessionId={}", request.getSessionId());
    }

    private void validateText(String text) {
        if(text == null || text.trim().isEmpty()){
            throw new IllegalArgumentException("번역할 텍스트가 없습니다.");
        }

        String trimmedText = text.trim();
        if(trimmedText.length() > MAX_TEXT_LENGTH){
            throw new IllegalArgumentException(
                    String.format("텍스트 길이는 %d자 이하여야 합니다.",
                            MAX_TEXT_LENGTH)
            );
        }
        // 악성 스크립트 기본 검증
        if(containSuspiciousCharacters(trimmedText)){
            throw new IllegalArgumentException("허용되지 않은 내용이 포함되어 있습니다.");
        }
    }

    private void validateTimestamp(Long timestamp) {
        if(timestamp == null){
            throw new IllegalArgumentException("타임스탬프는 필수입니다.");
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }

        // UUID 형식 검증
        if (!sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            throw new IllegalArgumentException("잘못된 세션 ID 형식입니다.");
        }
    }

    private void validateUserId(Long userId){
        if(userId == null){
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if(userId <= 0){
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 입니다.");
        }
    }

    private void validateLanguage(String language){
        if(language == null){
            return; // null => 기본값 사용
        }

        if(!LANGUAGE_PATTERN.matcher(language).matches()){
            throw new IllegalArgumentException("지원되지 않는 언어 코드입니다.");
        }
    }

    private boolean containSuspiciousCharacters(String text) {
        // 기본적인 악성 콘텐츠 검증 -> script injection 방지
        String lowerText = text.toLowerCase();
        String[] suspiciousPatterns = {
                "<script", "javascript:", "eval(", "alert(", "document.cookie"
        };

        for(String pattern : suspiciousPatterns){
            if(lowerText.contains(pattern)){
                return true;
            }
        }

        return false;
    }
}
