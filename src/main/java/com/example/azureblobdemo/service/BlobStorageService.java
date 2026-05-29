package com.example.azureblobdemo.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Azure Blob Storage 비즈니스 로직 서비스
 *
 * <p>Spring Cloud Azure가 자동 구성하는 {@link BlobServiceClient}를 주입받아
 * 지정된 컨테이너에 대한 업로드, 다운로드, 목록 조회, 삭제 기능을 제공합니다.
 *
 * <p>컨테이너 이름은 application.yml의 {@code app.azure.storage.container-name} 값을 사용하며,
 * 해당 컨테이너가 존재하지 않으면 애플리케이션 시작 시 자동으로 생성합니다.
 */
@Service
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    /**
     * @param blobServiceClient Spring Cloud Azure가 자동 구성하는 Blob 서비스 클라이언트
     * @param containerName     application.yml의 app.azure.storage.container-name 값
     */
    public BlobStorageService(
            BlobServiceClient blobServiceClient,
            @Value("${app.azure.storage.container-name}") String containerName) {
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // 컨테이너가 없으면 자동 생성
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    /**
     * 파일을 Azure Blob Storage에 업로드합니다.
     *
     * <p>동일한 이름의 Blob이 이미 존재하면 덮어씁니다.
     *
     * @param file 업로드할 멀티파트 파일
     * @return 업로드된 Blob의 공개 URL
     * @throws IOException 파일 스트림 읽기 실패 시
     */
    public String upload(MultipartFile file) throws IOException {
        String blobName = file.getOriginalFilename();
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true); // overwrite=true
        }
        return blobClient.getBlobUrl();
    }

    /**
     * 지정한 이름의 Blob을 다운로드합니다.
     *
     * @param blobName 다운로드할 Blob 이름 (파일명)
     * @return Blob 바이너리 데이터
     */
    public BinaryData download(String blobName) {
        return containerClient.getBlobClient(blobName).downloadContent();
    }

    /**
     * 지정한 Blob의 메타데이터(Content-Type 등)를 조회합니다.
     *
     * @param blobName 조회할 Blob 이름
     * @return Blob 속성 정보
     */
    public BlobProperties getProperties(String blobName) {
        return containerClient.getBlobClient(blobName).getProperties();
    }

    /**
     * 컨테이너 내 모든 Blob 이름을 목록으로 반환합니다.
     *
     * @return Blob 이름 목록
     */
    public List<String> list() {
        return containerClient.listBlobs()
                .stream()
                .map(BlobItem::getName)
                .toList();
    }

    /**
     * 지정한 이름의 Blob을 삭제합니다.
     *
     * @param blobName 삭제할 Blob 이름
     */
    public void delete(String blobName) {
        containerClient.getBlobClient(blobName).delete();
    }
}
