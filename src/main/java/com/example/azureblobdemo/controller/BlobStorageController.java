package com.example.azureblobdemo.controller;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobProperties;
import com.example.azureblobdemo.service.BlobStorageService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blobs")
public class BlobStorageController {

    private final BlobStorageService blobStorageService;

    public BlobStorageController(BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = blobStorageService.upload(file);
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully",
                "blobName", file.getOriginalFilename(),
                "url", url
        ));
    }

    @GetMapping("/download/{blobName}")
    public ResponseEntity<byte[]> download(@PathVariable String blobName) {
        BinaryData data = blobStorageService.download(blobName);
        BlobProperties props = blobStorageService.getProperties(blobName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(blobName).build());
        headers.setContentType(MediaType.parseMediaType(
                props.getContentType() != null ? props.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
        ));

        return ResponseEntity.ok().headers(headers).body(data.toBytes());
    }

    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(blobStorageService.list());
    }

    @DeleteMapping("/{blobName}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String blobName) {
        blobStorageService.delete(blobName);
        return ResponseEntity.ok(Map.of("message", "Blob deleted successfully", "blobName", blobName));
    }
}
