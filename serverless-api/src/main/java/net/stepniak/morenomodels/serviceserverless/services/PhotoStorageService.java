package net.stepniak.morenomodels.serviceserverless.services;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.enterprise.context.ApplicationScoped;
import java.net.URL;
import java.time.Duration;

@ApplicationScoped
public class PhotoStorageService {
    @ConfigProperty(name = "bucket.name")
    String bucketName;

    // TODO: quarkus-amazon-s3 is outdated. It doesnt provide the bean.
    S3Presigner s3Presigner = S3Presigner.create();

    public URL generatePresignedUploadURL(String storagePath) {
        return s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storagePath)
                        .build()
                )
                .signatureDuration(Duration.ofMinutes(10))
                .build()
        ).url();
    }

    public URL generatePresignedGetURL(String storagePath) {
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storagePath)
                        .build()
                )
                .signatureDuration(Duration.ofMinutes(10))
                .build()
        ).url();
    }
}
