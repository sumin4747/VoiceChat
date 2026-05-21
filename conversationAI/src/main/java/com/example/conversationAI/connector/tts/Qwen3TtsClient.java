package com.example.conversationAI.connector.tts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
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
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(600)) // 10분 타임아웃
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(600, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(600, TimeUnit.SECONDS))
                );

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
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