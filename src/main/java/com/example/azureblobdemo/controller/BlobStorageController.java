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

/**
 * Azure Blob Storage REST API 컨트롤러
 *
 * <p>모든 엔드포인트의 기본 경로는 {@code /api/blobs} 입니다.
 *
 * <ul>
 *   <li>POST   /api/blobs/upload          – 파일 업로드</li>
 *   <li>GET    /api/blobs/download/{name} – 파일 다운로드</li>
 *   <li>GET    /api/blobs                 – Blob 목록 조회</li>
 *   <li>DELETE /api/blobs/{name}          – Blob 삭제</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/blobs")
public class BlobStorageController {

    private final BlobStorageService blobStorageService;

    public BlobStorageController(BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    /**
     * 파일을 Azure Blob Storage에 업로드합니다.
     *
     * <p>요청 형식: {@code multipart/form-data}, 폼 필드명 {@code file}
     *
     * <p>예시 (curl):
     * <pre>
     *   curl -X POST http://localhost:8080/api/blobs/upload \
     *        -F "file=@/path/to/yourfile.jpg"
     * </pre>
     *
     * <p>응답 예시:
     * <pre>
     *   {
     *     "message": "File uploaded successfully",
     *     "blobName": "yourfile.jpg",
     *     "url": "https://youraccount.blob.core.windows.net/container/yourfile.jpg"
     *   }
     * </pre>
     *
     * @param file 업로드할 파일 (멀티파트 폼 데이터)
     * @return 업로드 결과 (메시지, Blob 이름, Azure URL)
     * @throws IOException 파일 읽기 오류 시
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = blobStorageService.upload(file);
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully",
                "blobName", file.getOriginalFilename(),
                "url", url
        ));
    }

    /**
     * 지정한 이름의 Blob을 다운로드합니다.
     *
     * <p>응답 헤더에 {@code Content-Disposition: attachment} 및 원본 {@code Content-Type}이 포함되어
     * 브라우저나 클라이언트가 파일로 저장할 수 있습니다.
     *
     * <p>예시 (curl):
     * <pre>
     *   curl -O -J http://localhost:8080/api/blobs/download/yourfile.jpg
     * </pre>
     *
     * @param blobName 다운로드할 Blob 이름 (URL 경로 변수)
     * @return 파일 바이트 배열 및 적절한 Content-Type/Content-Disposition 헤더
     */
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

    /**
     * 컨테이너 내 모든 Blob 이름을 목록으로 반환합니다.
     *
     * <p>예시 (curl):
     * <pre>
     *   curl http://localhost:8080/api/blobs
     * </pre>
     *
     * <p>응답 예시:
     * <pre>
     *   ["file1.jpg", "document.pdf", "data.csv"]
     * </pre>
     *
     * @return Blob 이름 문자열 배열
     */
    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(blobStorageService.list());
    }

    /**
     * 지정한 이름의 Blob을 삭제합니다.
     *
     * <p>예시 (curl):
     * <pre>
     *   curl -X DELETE http://localhost:8080/api/blobs/yourfile.jpg
     * </pre>
     *
     * <p>응답 예시:
     * <pre>
     *   {
     *     "message": "Blob deleted successfully",
     *     "blobName": "yourfile.jpg"
     *   }
     * </pre>
     *
     * @param blobName 삭제할 Blob 이름 (URL 경로 변수)
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{blobName}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String blobName) {
        blobStorageService.delete(blobName);
        return ResponseEntity.ok(Map.of("message", "Blob deleted successfully", "blobName", blobName));
    }
}
