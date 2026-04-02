package com.semotone.semotone.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.domain.ai.dto.AiResDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiClient {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=";

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 게시글 본문 텍스트를 제미나이 API로 분석하여 AiResDto로 반환
     *
     * 동기 방식으로 동작 (응답 올 때까지 블로킹)
     * 비동기 전환 시:
     *   - @Async 애노테이션을 메서드에 추가하고 반환 타입을 CompletableFuture<AiResDto>로 변경
     *   - 또는 PostService에서 별도 스케줄러/메시지 큐를 통해 비동기 처리
     */
    public AiResDto analyze(String text) {
        String url = GEMINI_API_URL + apiKey;

        // ObjectMapper로 요청 JSON 안전하게 직렬화 (수동 이스케이프 불필요)
        Map<String, Object> requestBody = buildRequestBody(text);
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("제미나이 요청 JSON 직렬화 실패: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        String responseBody = restTemplate.postForObject(url, entity, String.class);

        return parseResponse(responseBody);
    }

    /**
     * 제미나이 API 요청 바디 생성
     * JSON 구조: { "contents": [{ "parts": [{ "text": "..." }] }] }
     */
    private Map<String, Object> buildRequestBody(String text) {

        Map<String, Object> part = new HashMap<>();
        part.put("text", buildPrompt(text));

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(content));

        return request;
    }

    /**
     * 게시글 분석 프롬프트 생성
     * 응답이 JSON 형식으로만 오도록 명확하게 지정
     */
    private String buildPrompt(String text) {
        return """
                다음 심부름 게시글을 분석해서 아래 JSON 형식으로만 응답해줘. JSON 외에 다른 텍스트나 코드 블록은 포함하지 마.

                게시글: "%s"

                응답 형식:
                {
                  "category": "배달|구매|정보|기타 중 하나",
                  "object": "물건명 또는 null",
                  "type": "물건전달|음식배달|정보수집|기타 중 하나",
                  "urgency": "낮음|보통|높음 중 하나",
                  "fromLocation": "출발지 키워드 또는 null",
                  "toLocation": "도착지 키워드 또는 null",
                  "tags": {
                    "type": "위와 동일한 값",
                    "category": "위와 동일한 값",
                    "object": "위와 동일한 값",
                    "urgency": "위와 동일한 값"
                  }
                }
                """.formatted(text);
    }

    /**
     * 제미나이 응답 파싱 → AiResDto 변환
     * 응답에서 텍스트 추출 후 JSON 역직렬화
     */
    private AiResDto parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String rawText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // 제미나이가 ```json ... ``` 형태로 감싸서 응답하는 경우 제거
            String jsonText = rawText.trim();
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("(?s)```[a-zA-Z]*\\n?", "").replace("```", "").trim();
            }

            return objectMapper.readValue(jsonText, AiResDto.class);
        } catch (Exception e) {
            throw new RuntimeException("제미나이 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}
