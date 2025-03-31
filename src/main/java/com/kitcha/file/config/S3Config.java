package com.kitcha.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
    /**
     * S3 클라이언트 생성
     * 객체 업로드/다운로드 등의 작업에 사용
     *
     * @return S3 클라이언트 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder().region(Region.AP_NORTHEAST_2).build();
    }

    /**
     * 파일 다운로드 시 임시 접근 URL을 만들 때 사용
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
