package com.example.conversationAI.connector.tts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZonosClient {

    @Value("${zonos.api-key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://api.zyphra.com")
            .build();

    /**
     * 문장 단위로 분리해서 TTS 생성 후 FFmpeg으로 webm 합치기
     */
    public byte[] synthesizeSentences(String text, byte[] referenceAudioBytes, Map<String, Object> emotion) {
        String[] sentences = text.split("(?<=[.!?])\\s+");

        List<byte[]> chunks = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                byte[] chunk = synthesizeWebm(trimmed, referenceAudioBytes, emotion);
                if (chunk != null && chunk.length > 0) {
                    chunks.add(chunk);
                }
            }
        }

        if (chunks.isEmpty()) return new byte[0];
        if (chunks.size() == 1) return chunks.get(0);

        return mergeWithFfmpeg(chunks);
    }

    /**
     * webm 포맷으로 단일 TTS 생성
     */
    private byte[] synthesizeWebm(String text, byte[] referenceAudioBytes, Map<String, Object> emotion) {
        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("language_iso_code", "ko");
        body.put("speaking_rate", 18);
        body.put("model", "zonos-v0.1-transformer");
        body.put("emotion", emotion != null ? emotion : defaultEmotion());

        if (referenceAudioBytes != null) {
            body.put("speaker_audio", Base64.getEncoder().encodeToString(referenceAudioBytes));
        }

        return callApi(body);
    }

    /**
     * FFmpeg으로 webm 파일 합치기
     */
    private byte[] mergeWithFfmpeg(List<byte[]> chunks) {
        List<Path> tempFiles = new ArrayList<>();
        Path outputFile = null;

        try {
            // 각 청크를 임시 파일로 저장
            for (int i = 0; i < chunks.size(); i++) {
                Path tempFile = Files.createTempFile("tts_chunk_" + i + "_", ".webm");
                Files.write(tempFile, chunks.get(i));
                tempFiles.add(tempFile);
            }

            // FFmpeg concat 리스트 파일 생성
            Path listFile = Files.createTempFile("tts_list_", ".txt");
            StringBuilder listContent = new StringBuilder();
            for (Path tempFile : tempFiles) {
                listContent.append("file '").append(tempFile.toAbsolutePath()).append("'\n");
            }
            Files.write(listFile, listContent.toString().getBytes());
            tempFiles.add(listFile);

            // 출력 파일
            outputFile = Files.createTempFile("tts_merged_", ".webm");

            // FFmpeg 실행
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\ffmpeg-8.0.1-essentials_build\\bin\\ffmpeg.exe", "-y",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", listFile.toAbsolutePath().toString(),
                    "-c", "copy",
                    outputFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 합치기 실패, exit code: " + exitCode);
            }

            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("FFmpeg 합치기 실패: " + e.getMessage(), e);
        } finally {
            // 임시 파일 삭제
            for (Path tempFile : tempFiles) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
            if (outputFile != null) {
                try { Files.deleteIfExists(outputFile); } catch (IOException ignored) {}
            }
        }
    }

    private Map<String, Object> defaultEmotion() {
        Map<String, Object> emotion = new HashMap<>();
        emotion.put("happiness", 0.3);
        emotion.put("sadness", 0.15);
        emotion.put("disgust", 0.05);
        emotion.put("fear", 0.05);
        emotion.put("surprise", 0.05);
        emotion.put("anger", 0.05);
        emotion.put("other", 0.3);
        emotion.put("neutral", 0.7);
        return emotion;
    }

    private byte[] callApi(Map<String, Object> body) {
        try {
            return webClient.post()
                    .uri("/v1/audio/text-to-speech")
                    .header("X-API-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException(
                    "Zonos TTS 호출 실패 [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e
            );
        }
    }
}