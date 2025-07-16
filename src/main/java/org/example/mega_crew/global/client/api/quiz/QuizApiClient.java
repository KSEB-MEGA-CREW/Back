package org.example.mega_crew.global.client.api.quiz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Component
public class QuizApiClient {

  private static final String API_URL = "http://api.kcisa.kr/openapi/service/rest/meta13/getCTE01701";

  @Value("${sign.api.key}")
  private String apiKey;

  public List<Map<String, String>> fetchSignWords(int num) throws Exception {
    System.out.println("=== QuizApiClient.fetchSignWords() 호출됨 ===");
    RestTemplate restTemplate = new RestTemplate();
    String url = API_URL
        + "?serviceKey=" + apiKey
        + "&numOfRows=" + num
        + "&pageNo=1"
        + "&keyword=";

    System.out.println("=== QuizApiClient 요청 URL ===");
    System.out.println(url);
    System.out.println("API KEY: " + apiKey);

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    List<Map<String, String>> result = new ArrayList<>();

    if (response.getStatusCode() == HttpStatus.OK) {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode items = root.path("body").path("items");
      if (items.isArray()) {
        for (JsonNode item : items) {
          Map<String, String> map = new HashMap<>();
          map.put("word", item.path("word").asText());
          map.put("signDescription", item.path("signDescription").asText());
          map.put("category", item.path("category").asText());
          result.add(map);
        }
      }
    }
    return result;
  }
}
