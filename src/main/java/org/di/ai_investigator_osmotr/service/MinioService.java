package org.di.ai_investigator_osmotr.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final FileCipher fileCipher;

    @Value("${minio.bucket.name:cases}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("Created bucket: {}", bucketName);
        }
    }
    private String extractObjectNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
    }

    public InputStream downloadFile(String fileUrl) {
        try {
            String objectName = extractObjectNameFromUrl(fileUrl);
            InputStream raw = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            if (fileCipher.isEnabled() && fileCipher.isEncryptedName(objectName)) {
                log.info("🔓 Decrypting file: {}", objectName);
                byte[] decrypted = fileCipher.decrypt(raw.readAllBytes());
                return new ByteArrayInputStream(decrypted);
            }

            return raw;

        } catch (Exception e) {
            log.error("Error downloading file from Minio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file", e);
        }
    }
}
