package com.example.conversationAI.connector.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Map;

@Component
public class WhisperClient {

    @Value("${openai.api-key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com")
            .build();

    /**
     * @param audioFile 사용자가 업로드한 음성 파일
     * @return 변환된 텍스트
     */
    public String transcribe(MultipartFile audioFile) {
        try {
            byte[] bytes = audioFile.getBytes();
            String filename = audioFile.getOriginalFilename() != null
                    ? audioFile.getOriginalFilename()
                    : "audio.webm";
            return transcribeBytes(bytes, filename);
        } catch (IOException e) {
            throw new RuntimeException("음성 파일 읽기 실패", e);
        }
    }

    public String transcribeBytes(byte[] audioBytes, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() { return filename; }
        }).contentType(MediaType.APPLICATION_OCTET_STREAM);

        builder.part("model", "whisper-1");
        builder.part("language", "ko");

        try {
            Map<?, ?> response = webClient.post()
                    .uri("/v1/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("text")) {
                throw new RuntimeException("Whisper 응답에 text 필드 없음");
            }

            return response.get("text").toString().trim();

        } catch (WebClientResponseException e) {
            throw new RuntimeException(
                    "Whisper STT 호출 실패 [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e
            );
        }
    }
}