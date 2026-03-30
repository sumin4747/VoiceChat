package com.example.conversationAI.connector.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.conversationAI.chat.domain.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    /**
     * 텍스트 응답 + 감정 수치를 함께 반환.
     */
    public GeminiResult generateWithHistoryAndEmotion(
            String systemInstruction,
            List<ChatMessage> history,
            String newMessage
    ) {
        String emotionInstruction = systemInstruction + """

                [응답 형식 - 반드시 아래 JSON만 출력, 다른 텍스트 없이]
                {
                  "reply": "실제 대화 응답 텍스트",
                  "emotion": {
                    "happiness": 0.0~0.4,
                    "sadness": 0.0~0.4,
                    "disgust": 0.0~0.1,
                    "fear": 0.0~0.15,
                    "surprise": 0.0~0.4,
                    "anger": 0.0~0.4,
                    "other": 0.2~0.4,
                    "neutral": 0.0~1.0
                  }
                }
                대화 맥락에 맞게 감정 수치를 자연스럽게 설정해줘.
                위로할 때는 sadness+neutral 높게, 반가울 때는 happiness 높게,
                슬픈 소식엔 sadness 높게, 화가 날 땐 anger 조금 높게.
                """;

        List<Map<String, Object>> contents = new ArrayList<>();

        for (ChatMessage msg : history) {
            String role = msg.getRole() == ChatMessage.Role.USER ? "user" : "model";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", newMessage))
        ));

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", emotionInstruction))
                ),
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.8,
                        "maxOutputTokens", 600,
                        "responseMimeType", "application/json"
                )
        );

        String rawText = callApi(body);

        try {
            String cleaned = rawText.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("(?s)```\\s*$", "")
                    .trim();

            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);
            String reply = (String) parsed.get("reply");
            Map<String, Object> emotionMap = (Map<String, Object>) parsed.get("emotion");

            return new GeminiResult(reply, emotionMap);

        } catch (Exception e) {
            System.err.println("Gemini 감정 JSON 파싱 실패, 기본 감정 사용: " + e.getMessage());
            return new GeminiResult(rawText, defaultEmotion());
        }
    }

    private String callApi(Map<String, Object> body) {
        Map response = webClient.post()
                .uri("/v1beta/models/" + model + ":generateContent?key=" + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new IllegalStateException("Gemini API 응답이 null입니다.");

        List candidates = (List) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) throw new IllegalStateException("Gemini API 응답에 candidates가 없습니다.");

        Map first = (Map) candidates.get(0);
        Map content = (Map) first.get("content");
        List parts = (List) content.get("parts");
        Map textPart = (Map) parts.get(0);

        return textPart.get("text").toString();
    }

    private Map<String, Object> defaultEmotion() {
        return Map.of(
                "happiness", 0.3,
                "sadness", 0.15,
                "disgust", 0.05,
                "fear", 0.05,
                "surprise", 0.05,
                "anger", 0.05,
                "other", 0.3,
                "neutral", 0.7
        );
    }

    public record GeminiResult(String reply, Map<String, Object> emotion) {}
}