package com.tondise.utils.storage.s3;

import com.tondise.utils.storage.Storage;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
public class MinioS3AutoConfiguration {

    @Bean
    public Storage storage(
            @Value("${s3.bucket.name}") String bucketName,
            @Value("${s3.folder}") String folder,
            MinioClient minioClient) {
        return new S3Storage(minioClient, bucketName, folder);
    }

    @Bean
    public MinioClient minioClient(
            @Value("${s3.access.name}") String accessKey,
            @Value("${s3.access.secret}") String accessSecret,
            @Value("${s3.url}") String s3Url) {
        OkHttpClient httpClient =
                new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.MINUTES)
                        .writeTimeout(10, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.MINUTES)
                        .build();
        return MinioClient.builder()
                .endpoint(s3Url)
                .httpClient(httpClient)
                .credentials(accessKey, accessSecret)
                .build();
    }
}
