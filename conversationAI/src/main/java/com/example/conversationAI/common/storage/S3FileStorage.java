package com.example.conversationAI.common.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Component
@Primary
public class S3FileStorage implements FileStorage {

    private final AmazonS3 amazonS3;
    private final String bucket;

    public S3FileStorage(
            AmazonS3 amazonS3,
            @Value("${cloud.aws.s3.bucket}") String bucket
    ) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
    }

    /** TTS 결과 음성 파일 저장 */
    @Override
    public String uploadTtsResult(Long voiceModelId, byte[] audioBytes, String extension) {
        String key = "tts/" + voiceModelId + "/" + UUID.randomUUID() + "." + extension;
        return upload(key, audioBytes, "audio/wav");
    }

    /** 녹음 파일 저장 */
    @Override
    public String uploadRecording(Long userId, String sentenceId, MultipartFile file) throws IOException {
        String key = "recordings/" + userId + "/wavs/" + sentenceId + ".wav";
        return upload(key, file.getBytes(), "audio/wav");
    }

    private String upload(String key, byte[] bytes, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);

        amazonS3.putObject(new PutObjectRequest(
                bucket,
                key,
                new ByteArrayInputStream(bytes),
                metadata
        ));

        return amazonS3.getUrl(bucket, key).toString();
    }
}
