package org.example.mega_crew.global.client.api.quiz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

@Component
public class QuizApiClient {

  @Value("${sign.api.url}")
  private String API_URL;

  @Value("${sign.api.key}")
  private String apiKey;

  public List<Map<String, String>> fetchSignWords(int num) throws Exception {
    RestTemplate restTemplate = new RestTemplate();

    // 전체 페이지 계산
    int totalCount = 3619;
    int numOfRows = 20;
    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

    // 범위 내 랜덤 pageNo 선택
    Random rand = new Random();
    int pageNo = rand.nextInt(totalPages) + 1; // 1부터 181까지

    String url = API_URL
        + "?serviceKey=" + apiKey
        + "&numOfRows=" + num
        + "&pageNo=" + pageNo
        + "&keyword=";

    System.out.println("=== QuizApiClient 요청 URL ===");
    System.out.println(url + " (pageNo=" + pageNo + ")");
    System.out.println("API KEY: " + apiKey);

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    List<Map<String, String>> result = new ArrayList<>();

    if (response.getStatusCode() == HttpStatus.OK) {
      XmlMapper xmlMapper = new XmlMapper();
      JsonNode root = xmlMapper.readTree(response.getBody());
      JsonNode itemsNode = root.path("body").path("items").path("item");

      if (itemsNode.isArray()) {
        for (JsonNode item : itemsNode) {
          result.add(parseItem(item));
        }
      }
      else if (itemsNode.isObject()) {
        result.add(parseItem(itemsNode));
      } else {
        System.out.println("items 노드가 존재하지 않거나 비어있음: " + response.getBody());
      }
    } else {
      System.out.println("API 응답 코드: " + response.getStatusCode());
    }
    return result;
  }

  // 각 item에서 원하는 값 안전하게 추출 (빈 값도 ""로)
  private Map<String, String> parseItem(JsonNode item) {
    Map<String, String> map = new HashMap<>();
    map.put("word", getSafeText(item, "title"));
    map.put("signDescription", getSafeText(item, "signDescription"));
    map.put("category", getSafeText(item, "categoryType")); // categoryType으로 변경!
    map.put("subDescription", getSafeText(item, "subDescription"));
    return map;
  }

  // 필드 누락
  private String getSafeText(JsonNode node, String field) {
    JsonNode valueNode = node.get(field);
    if (valueNode == null || valueNode.isNull()) return "";
    String value = valueNode.asText();
    return value == null ? "" : value;
  }
}
