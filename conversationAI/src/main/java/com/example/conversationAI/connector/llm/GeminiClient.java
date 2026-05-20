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
                        "maxOutputTokens",  8192,
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
        int maxRetries = 3;
        int retryDelayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map response = webClient.post()
                        .uri("/v1/models/" + model + ":generateContent?key=" + apiKey)
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

            } catch (Exception e) {
                boolean isRetryable = e.getMessage() != null &&
                        (e.getMessage().contains("503") || e.getMessage().contains("429"));

                if (isRetryable && attempt < maxRetries) {
                    System.out.println("[RETRY] Gemini 호출 실패 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
                }
            }
        }
        throw new IllegalStateException("Gemini API 재시도 모두 실패");
    }

    public boolean isDepressed(String message) {
        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text",
                                "너는 감정 분석 AI야. 아래 기준으로만 판단해.\n" +
                                        "true를 출력하는 경우: 무기력함, 지속적인 슬픔, 삶의 의미 상실, 공허함, " +
                                        "아무것도 하기 싫다는 표현, 오래 지속되는 절망감, 살기 싫다, 사라지고 싶다, " +
                                        "존재 자체에 대한 부정적 표현이 느껴질 때.\n" +
                                        "false를 출력하는 경우: 특정 사건으로 인한 일시적 감정, 단순 스트레스, " +
                                        "가벼운 피로감, 오늘 힘들었다는 정도의 가벼운 표현, 긍정적 내용이 포함된 경우.\n" +
                                        "true 또는 false만 출력해. 다른 말은 절대 하지 마."
                        ))
                ),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", message))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.0,
                        "maxOutputTokens", 10,
                        "thinkingConfig", Map.of("thinkingBudget", 0)  // thinking 비활성화
                )
        );

        try {
            Map response = webClient.post()
                    .uri("/v1/models/" + model + ":generateContent?key=" + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return false;

            List candidates = (List) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return false;

            Map first = (Map) candidates.get(0);
            Map content = (Map) first.get("content");
            if (content == null) return false;

            List parts = (List) content.get("parts");
            if (parts == null || parts.isEmpty()) return false;

            Map textPart = (Map) parts.get(0);
            String result = textPart.get("text").toString().trim().toLowerCase();
            return result.contains("true");

        } catch (Exception e) {
            System.err.println("[DEPRESSION GEMINI] 분석 실패: " + e.getMessage());
            return false;
        }
    }

    public record GeminiResult(String reply, String instruct) {}
}