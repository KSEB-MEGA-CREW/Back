package org.example.mega_crew.domain.media.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mega_crew.domain.media.dto.request.TextTo3DRequest;
import org.example.mega_crew.domain.media.dto.response.TextTo3DResponse;
import org.example.mega_crew.global.client.ai.AiServerClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextTo3DService {

    private final AiServerClient aiServerClient;

    public TextTo3DResponse generate3D(TextTo3DRequest request){
        try{
            log.info("AI server generate 3D response");
            TextTo3DResponse aiResponse = aiServerClient.generate3DFromText(request);

            if(aiResponse!= null){
                log.info("AI server generate 3D response success");
                return TextTo3DResponse.success(aiResponse.getAiResult());
            }
            else{
                log.error("AI server generate 3D response failed");
                return TextTo3DResponse.failure("AI server generate 3D response failed");
            }
        } catch(Exception e){
            log.error(e.getMessage());
            return TextTo3DResponse.failure("AI server generate 3D response failed");
        }
    }
}
