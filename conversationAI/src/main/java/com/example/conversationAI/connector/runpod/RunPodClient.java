package com.example.conversationAI.connector.runpod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class RunPodClient {

    private final WebClient webClient;

    public RunPodClient(@Value("${runpod.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * RunPod에 학습 시작 요청
     * @param voiceId 학습 완료 후 callback에 사용할 voiceId
     * @param userId  녹음 파일 경로 찾기 위한 userId
     */
    public void startTraining(Long voiceId, Long userId) {
        Map<String, Object> body = Map.of(
                "voiceId", voiceId,
                "userId", userId
        );

        webClient.post()
                .uri("/train")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        null,
                        error -> System.err.println("RunPod 학습 시작 실패: " + error.getMessage())
                );
    }
}