package com.ddogalmap.domain.chat.service;

import com.ddogalmap.domain.chat.dto.groupChat.image.UrlDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUtilService {

    private static final String DEFAULT_IMAGE = "chat/default_image.png";
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.cloud.aws.s3.cloudfront-url}")
    private String domain;

    //presignedUrl 발급
    public UrlDto generatePresignedUrl(String folder, String originalFilename) {
        //파일 이름 형식 예외처리
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));  //확장자
        }
        String key = folder + "/" + UUID.randomUUID() + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        //presigned url 발급
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build()
        );
        return new UrlDto(presignedRequest.url().toString(), key);
    }

    //최종 이미지 url 반환
    public String getImageUrl(String key) {
        if (key == null || key.isEmpty()) {
            key = DEFAULT_IMAGE;
        }
        // domain 끝 슬래시 제거
        String normalizedDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        // key 앞에 슬래시 보장
        String normalizedKey = key.startsWith("/") ? key : "/" + key;
        return normalizedDomain + normalizedKey;
    }

    //S3 이미지 삭제
    public void deleteS3Image(String key) {
        if (key == null || key.isEmpty() || key.equals(DEFAULT_IMAGE)) {  //기본 이미지면 삭제x
            return;
        }
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
}
