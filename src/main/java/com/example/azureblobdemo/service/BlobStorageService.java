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

@Service
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
            BlobServiceClient blobServiceClient,
            @Value("${app.azure.storage.container-name}") String containerName) {
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    public String upload(MultipartFile file) throws IOException {
        String blobName = file.getOriginalFilename();
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true);
        }
        return blobClient.getBlobUrl();
    }

    public BinaryData download(String blobName) {
        return containerClient.getBlobClient(blobName).downloadContent();
    }

    public BlobProperties getProperties(String blobName) {
        return containerClient.getBlobClient(blobName).getProperties();
    }

    public List<String> list() {
        return containerClient.listBlobs()
                .stream()
                .map(BlobItem::getName)
                .toList();
    }

    public void delete(String blobName) {
        containerClient.getBlobClient(blobName).delete();
    }
}
