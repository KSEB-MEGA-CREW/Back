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
      XmlMapper xmlMapper = new XmlMapper();
      JsonNode root = xmlMapper.readTree(response.getBody());
      // 실제 구조: <response><body><items><item>...
      JsonNode itemsNode = root.path("body").path("items").path("item");

      // 여러 개일 때 (배열)
      if (itemsNode.isArray()) {
        for (JsonNode item : itemsNode) {
          result.add(parseItem(item));
        }
      }
      // 한 개만 있을 때 (객체)
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
    return map;
  }

  // 필드 누락/빈 값 대응 함수
  private String getSafeText(JsonNode node, String field) {
    JsonNode valueNode = node.get(field);
    if (valueNode == null || valueNode.isNull()) return "";
    String value = valueNode.asText();
    return value == null ? "" : value;
  }
}
