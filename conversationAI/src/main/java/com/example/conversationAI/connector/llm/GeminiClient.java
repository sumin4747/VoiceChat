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

    public GeminiResult generateWithHistoryAndEmotion(
            String systemInstruction,
            List<ChatMessage> history,
            String newMessage
    ) {
        String fullInstruction = systemInstruction + """

                [응답 형식 - 반드시 아래 JSON만 출력, 다른 텍스트 없이]
                {
                  "reply": "실제 대화 응답 텍스트",
                  "instruct": "TTS 음성 톤 지시문 (영어)"
                }

                instruct 선택 기준 (대화 맥락에 맞게 자연스럽게 선택):
                - "Soft and comforting tone."  → 위로, 슬픔 공감할 때
                - "Warm and gentle tone."      → 따뜻하게 격려할 때
                - "Calm and steady tone."      → 차분하게 조언할 때
                - "Bright and warm tone."      → 기쁜 소식, 응원할 때
                - "Gentle and reassuring tone." → 불안해하거나 걱정할 때
                - "Gentle tone."               → 일반적인 대화
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
                        "parts", List.of(Map.of("text", fullInstruction))
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
            String instruct = (String) parsed.getOrDefault("instruct", "Gentle tone.");

            return new GeminiResult(reply, instruct);

        } catch (Exception e) {
            System.err.println("Gemini JSON 파싱 실패: " + e.getMessage());
            return new GeminiResult(rawText, "Gentle tone.");
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

    public record GeminiResult(String reply, String instruct) {}
}