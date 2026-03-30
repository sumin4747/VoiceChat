package com.example.conversationAI.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로컬 디스크에 저장된 음성 파일을 HTTP로 서빙.
 *
 * 예) storage.base-path = /tmp/conversationai
 *     저장 파일: /tmp/conversationai/voices/tts/1/abc.webm
 *     접근 URL : http://localhost:8080/files/voices/tts/1/abc.webm
 *
 * AWS S3 연동 후에는 이 설정 불필요 (S3 URL 직접 사용).
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${storage.base-path}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /files/** 요청 → 로컬 디스크 파일 서빙
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + basePath + "/");
    }
}