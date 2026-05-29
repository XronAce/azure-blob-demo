package com.example.azureblobdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Azure Blob Storage REST API 데모 애플리케이션
 *
 * <p>이 애플리케이션은 Azure Blob Storage를 활용한 파일 업로드/다운로드 REST API를 제공합니다.
 * Spring Cloud Azure SDK를 사용하며, application.yml에 Azure 연결 정보를 설정합니다.
 *
 * <p>실행 방법:
 * <pre>
 *   ./gradlew bootRun
 * </pre>
 *
 * <p>기본 포트: application.yml의 server.port 값 (기본 8080)
 */
@SpringBootApplication
public class AzureBlobDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureBlobDemoApplication.class, args);
    }
}
