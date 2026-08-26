package com.tondise.ecommerce.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.tondise.ecommerce.dao.dto.DocumentDto;
import com.tondise.utils.storage.Storage;
import com.tondise.utils.storage.s3.S3Storage;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Test d'intégration réel contre MinIO (comme {@code EcommerceApplicationTests}
 * nécessite un Postgres accessible, celui-ci nécessite un MinIO accessible —
 * variables d'environnement {@code MINIO_URL}/{@code MINIO_ACCESS_KEY}/
 * {@code MINIO_SECRET_KEY}/{@code MINIO_BUCKET}, mêmes noms et mêmes valeurs
 * par défaut que {@code application-dev.yml}). Ne mocke pas {@link Storage} :
 * l'objectif est de vérifier que l'upload écrit vraiment dans le bucket et
 * que le lien signé renvoyé est vraiment téléchargeable, pas seulement que
 * les bonnes méthodes sont appelées.
 */
class DocumentServiceTest {

    private static final String FOLDER = "test-documents";

    private DocumentService documentService;
    private Storage storage;
    private String createdDocumentId;

    @BeforeEach
    void setUp() throws Exception {
        String url = System.getenv().getOrDefault("MINIO_URL", "http://localhost:9000");
        String accessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "tondise");
        String secretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "tondise123");
        String bucket = System.getenv().getOrDefault("MINIO_BUCKET", "tondise");

        MinioClient minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        storage = new S3Storage(minioClient, bucket, FOLDER);
        documentService = new DocumentService(storage);
    }

    @AfterEach
    void tearDown() {
        if (createdDocumentId != null) {
            storage.delete(createdDocumentId);
        }
    }

    @Test
    void upload_writesToMinioAndReturnsAWorkingDownloadLink() throws Exception {
        byte[] content = "contenu de test tondise".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.txt", "text/plain", content);

        DocumentDto uploaded = documentService.upload(file);
        createdDocumentId = uploaded.getId();

        assertThat(uploaded.getId()).isNotBlank().endsWith(".txt");
        assertThat(uploaded.getUrl()).isNotBlank().startsWith("http");
        assertThat(uploaded.getContentType()).isEqualTo("text/plain");

        assertThat(fetch(uploaded.getUrl())).isEqualTo(content);
    }

    @Test
    void getById_regeneratesAWorkingLinkForAnAlreadyUploadedDocument() throws Exception {
        byte[] content = "image simulée".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "product.png", "image/png", content);
        createdDocumentId = documentService.upload(file).getId();

        // Simule un front qui n'a conservé que l'id (le lien signé de l'upload a expiré)
        // et rappelle GET /documents/{id} plus tard pour en obtenir un nouveau.
        DocumentDto refetched = documentService.getById(createdDocumentId);

        assertThat(refetched.getId()).isEqualTo(createdDocumentId);
        assertThat(refetched.getUrl()).isNotBlank().startsWith("http");
        assertThat(fetch(refetched.getUrl())).isEqualTo(content);
    }

    private byte[] fetch(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }
}
