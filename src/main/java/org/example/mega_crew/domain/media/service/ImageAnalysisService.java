package org.example.mega_crew.domain.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.media.dto.request.WebcamAnalysisRequest;
import org.example.mega_crew.domain.media.dto.response.WebcamAnalysisResponse;
import org.example.mega_crew.global.client.ai.AiServerClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageAnalysisService {

    private final AiServerClient aiServerClient;

    public WebcamAnalysisResponse analyzeImage(WebcamAnalysisRequest request) {
        try{
            log.info("AI server generate 3D request: {}", request.getImageFile().getOriginalFilename());

            WebcamAnalysisResponse aiResponse = aiServerClient.analysisImage(request.getImageFile(), request);

            if(aiResponse!=null){
                log.info("AI server generate 3D response: {}", aiResponse.getAnalysisResult());
                return WebcamAnalysisResponse.success(aiResponse.getAnalysisResult());
            }
            else{
                log.info("AI server generate 3D response failed");
                return WebcamAnalysisResponse.error("AI server generate 3D response failed");
            }
        } catch(Exception e){
            log.error(e.getMessage());
            return WebcamAnalysisResponse.error("AI server generate 3D response failed");
        }
    }

}
