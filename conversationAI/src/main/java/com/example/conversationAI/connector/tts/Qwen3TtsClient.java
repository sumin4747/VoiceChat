package com.example.conversationAI.connector.tts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class Qwen3TtsClient implements TtsClient {

    private final WebClient webClient;
    private final String speaker;

    public Qwen3TtsClient(
            @Value("${qwen3-tts.url}") String baseUrl,
            @Value("${qwen3-tts.speaker:my_voice}") String speaker
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB로 늘리기
                .build();
        this.speaker = speaker;
    }

    @Override
    public byte[] synthesize(String text, byte[] referenceAudio, String instruct) {
        return synthesize(text, referenceAudio, instruct, null);
    }

    public byte[] synthesize(String text, byte[] referenceAudio, String instruct, String modelPath) {
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("speaker", speaker);
        body.put("instruct", instruct != null ? instruct : "Gentle tone.");
        if (modelPath != null) {
            body.put("modelPath", modelPath);
        }

        return webClient.post()
                .uri("/tts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}