package com.tondise.utils.storage.s3;

import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ResourceNotFoundException;
import com.tondise.utils.storage.Storage;
import com.tondise.utils.storage.utils.Utils;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
@FieldNameConstants
public class S3Storage implements Storage {
    private final MinioClient minioClient;
    private final String bucketName;
    private final String folder;

    public String download(String filename) {
            String objectName = folder + "/" + filename;
        try {
            // Valid for 1 hour
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(60 * 60) // Valid for 1 hour
                            .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new ResourceNotFoundException("No file Found", e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(computeDocumentName(filename)).build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType(String type) {
        return new Utils().getContentTypeValue(type);
    }

    @Override
    public void save(String filename, String type, InputStream file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .object(computeDocumentName(filename))
                            .contentType(getContentType(type))
                            .bucket(bucketName)
                            .stream(file, file.available(), -1)
                            .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private String computeDocumentName(String filename) {
        return Optional.ofNullable(folder)
                .map(value -> value.concat("/").concat(filename))
                .orElse(filename);
    }


}
