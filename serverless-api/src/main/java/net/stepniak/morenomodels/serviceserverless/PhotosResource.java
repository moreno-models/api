package net.stepniak.morenomodels.serviceserverless;

import net.stepniak.morenomodels.serviceserverless.exceptions.NotFoundException;
import net.stepniak.morenomodels.serviceserverless.generated.PhotosApi;
import net.stepniak.morenomodels.serviceserverless.generated.model.*;
import net.stepniak.morenomodels.serviceserverless.repositories.ModelsRepository;
import net.stepniak.morenomodels.serviceserverless.repositories.PhotosRepository;
import net.stepniak.morenomodels.serviceserverless.services.PhotoStorageService;
import net.stepniak.morenomodels.serviceserverless.tables.records.ModelsRecord;
import net.stepniak.morenomodels.serviceserverless.tables.records.PhotosRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.Collectors;

public class PhotosResource implements PhotosApi {
    @Inject
    PhotosRepository photosRepository;

    @Inject
    ModelsRepository modelsRepository;

    @Inject
    PhotoStorageService photoStorageService;

    @Inject
    S3Client s3Client;

    @ConfigProperty(name = "bucket.name")
    String bucketName;


    @Override
    public void archivePhoto(String photoSlug, Boolean delete) {
        if (delete != null && delete) {
            PhotosRecord photo = photosRepository.deleteByPhotoSlug(photoSlug)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Photo with slug [%s] not found", photoSlug)
                    ));
            s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(photo.getUri())
                    .build()
            );
        } else {
            photosRepository.update(photoSlug, PhotosRepository.UpdatablePhoto.builder()
                    .archived(true)
                    .build()
            );
        }
    }

    @Override
    public CreatedPhoto createPhoto(NewPhoto newPhoto) {
        PhotosRecord photo = photosRepository.createPhoto(newPhoto);
        return CreatedPhoto.builder()
                .photoId(photo.getPhotoId())
                .photoSlug(photo.getPhotoSlug())
                .uploadUri(
                        photoStorageService.generatePresignedUploadURL(
                                photo.getUri()
                        ).toString()
                )
                .build();
    }

    @Override
    public Photo getPhoto(String photoSlug) {
        return photosRepository.findByPhotoSlug(photoSlug)
                .map(this::toApiModel)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Photo with slug [%s] not found", photoSlug)
                ));
    }

    private Photo toApiModel(PhotosRecord photosRecord) {
        return Photo.builder()
                .photoId(photosRecord.getPhotoId())
                .photoSlug(photosRecord.getPhotoSlug())
                .width(photosRecord.getWidth())
                .height(photosRecord.getHeight())
                .modelSlug(photosRecord.getModelId() != null ?
                        // TODO: this may not be the best idea.
                        // performs millions queries when listing photos
                        modelsRepository.findByModelId(photosRecord.getModelId())
                                .map(ModelsRecord::getModelId).orElse(null)
                        : null
                )
                .uri(photoStorageService.generatePresignedGetURL(photosRecord.getUri()).toString())
                .archived(photosRecord.getArchived())
                .created(photosRecord.getCreated())
                .updated(photosRecord.getUpdated())
                .version(photosRecord.getVersion())
                .build();
    }

    @Override
    public Photos listPhotos(String nextToken, Integer pageSize, Boolean showArchived, String modelSlug) {
        if (pageSize == null) {
            pageSize = 20;
        }
        PhotosRepository.PhotosPage page = photosRepository.list(
                nextToken,
                pageSize,
                PhotosRepository.PhotosFilters.builder()
                        .showArchived(showArchived)
                        .modelSlug(modelSlug)
                        .build()
        );

        return Photos.builder()
                .items(page.getPhotos().stream().map(this::toApiModel).collect(Collectors.toList()))
                .metadata(PaginationMetadata.builder()
                        .nextToken(page.getNextToken())
                        .build()
                ).build();
    }

    @Override
    public Photo uploadPhoto(String photoSlug, File body) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED AND WON'T BE.");
    }
}
