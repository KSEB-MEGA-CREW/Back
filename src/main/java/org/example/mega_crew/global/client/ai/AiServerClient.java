package org.example.mega_crew.global.client.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.media.dto.request.TextTo3DRequest;
import org.example.mega_crew.domain.media.dto.request.WebcamAnalysisRequest;
import org.example.mega_crew.domain.media.dto.response.TextTo3DResponse;
import org.example.mega_crew.domain.media.dto.response.WebcamAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@Component
@RequiredArgsConstructor
@Slf4j
public class AiServerClient {
    private final RestTemplate restTemplate;

    @Value("${ai.server.base-url}")
    private String aiServerBaseUrl;

    public WebcamAnalysisResponse analysisImage(MultipartFile file, WebcamAnalysisRequest request){
        String url = aiServerBaseUrl+"/analysis/webcam";

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<WebcamAnalysisResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, WebcamAnalysisResponse.class
            );

            return response.getBody();
        } catch (Exception ex){
            log.error("AI server webcam analysis request error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public TextTo3DResponse generate3DFromText(TextTo3DRequest request){
        String url = aiServerBaseUrl+"/generate/3d";

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TextTo3DRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TextTo3DResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST,entity, TextTo3DResponse.class
            );

            return response.getBody();
        } catch(Exception ex){
            log.error("AI server generate 3D request error: {}", ex.getMessage(), ex);
            return null;
        }
    }
}
