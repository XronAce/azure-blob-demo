# Azure Blob Storage REST API

Spring Boot 3.5.3 + Spring Cloud Azure SDK를 사용한 Azure Blob Storage 파일 업로드/다운로드 REST API입니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 17 |
| Spring Boot | 3.5.3 |
| Spring Cloud Azure | 5.22.0 |
| Gradle (wrapper) | 8.14 |

---

## 시작하기

### 1. 사전 요구사항

- **JDK 17** 이상
- **Azure Storage 계정** ([Azure 포털](https://portal.azure.com)에서 생성)

### 2. 설정 파일 생성

`src/main/resources/application-example.yml`을 복사해서 `application.yml`을 만들고 본인의 Azure 정보를 입력합니다.

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
```

그 다음 `application.yml`을 열어 아래 값을 채웁니다:

```yaml
spring:
  cloud:
    azure:
      storage:
        blob:
          # Azure 포털 > 스토리지 계정 > 보안 + 네트워킹 > 액세스 키에서 복사
          connection-string: DefaultEndpointsProtocol=https;AccountName=YOUR_ACCOUNT_NAME;AccountKey=YOUR_ACCOUNT_KEY;EndpointSuffix=core.windows.net
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

app:
  azure:
    storage:
      container-name: YOUR_CONTAINER_NAME  # 없으면 자동 생성됨

server:
  port: 8080
```

> **주의:** `application.yml`에는 실제 액세스 키가 포함되므로 절대 Git에 커밋하지 마세요.  
> 이 파일은 `.gitignore`에 등록되어 있습니다.

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

서버가 시작되면 `http://localhost:8080` (또는 설정한 포트)에서 API를 사용할 수 있습니다.

---

## API 사용법

### 파일 업로드

```
POST /api/blobs/upload
Content-Type: multipart/form-data
```

| 파라미터 | 형식 | 설명 |
|---------|------|------|
| `file` | File | 업로드할 파일 |

```bash
curl -X POST http://localhost:8080/api/blobs/upload \
     -F "file=@/path/to/yourfile.jpg"
```

**응답 예시:**
```json
{
  "message": "File uploaded successfully",
  "blobName": "yourfile.jpg",
  "url": "https://youraccount.blob.core.windows.net/container/yourfile.jpg"
}
```

---

### 파일 다운로드

```
GET /api/blobs/download/{blobName}
```

```bash
# 파일 내용 출력
curl http://localhost:8080/api/blobs/download/yourfile.jpg

# 파일로 저장
curl -O -J http://localhost:8080/api/blobs/download/yourfile.jpg
```

응답 헤더에 `Content-Disposition: attachment`와 원본 `Content-Type`이 포함됩니다.

---

### Blob 목록 조회

```
GET /api/blobs
```

```bash
curl http://localhost:8080/api/blobs
```

**응답 예시:**
```json
["yourfile.jpg", "document.pdf", "data.csv"]
```

---

### Blob 삭제

```
DELETE /api/blobs/{blobName}
```

```bash
curl -X DELETE http://localhost:8080/api/blobs/yourfile.jpg
```

**응답 예시:**
```json
{
  "message": "Blob deleted successfully",
  "blobName": "yourfile.jpg"
}
```

---

## Postman 컬렉션

`azure-blob-demo.postman_collection.json` 파일을 Postman에서 Import 하면 4개의 API를 바로 테스트할 수 있습니다.

1. Postman 실행 → **Import** 클릭
2. `azure-blob-demo.postman_collection.json` 파일 선택
3. Import 완료 후 **Upload** 요청부터 실행

> Upload 실행 시 `{{blobName}}` 변수가 자동으로 설정되어 Download, Delete 요청에 바로 사용됩니다.

---

## 연결 방법 (application.yml)

### 방법 1: 연결 문자열 (권장)

Azure 포털 > 스토리지 계정 > **보안 + 네트워킹** > **액세스 키** > `연결 문자열` 복사

```yaml
spring:
  cloud:
    azure:
      storage:
        blob:
          connection-string: DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net
```

### 방법 2: 계정 이름 + 키 분리 입력

```yaml
spring:
  cloud:
    azure:
      storage:
        blob:
          account-name: YOUR_ACCOUNT_NAME
          account-key: YOUR_ACCOUNT_KEY
```
