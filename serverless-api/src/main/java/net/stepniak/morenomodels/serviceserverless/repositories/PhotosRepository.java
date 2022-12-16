package net.stepniak.morenomodels.serviceserverless.repositories;

import lombok.Builder;
import lombok.Value;
import lombok.val;
import net.stepniak.morenomodels.serviceserverless.Tables;
import net.stepniak.morenomodels.serviceserverless.exceptions.DataExpiredException;
import net.stepniak.morenomodels.serviceserverless.exceptions.NotFoundException;
import net.stepniak.morenomodels.serviceserverless.generated.model.NewPhoto;
import net.stepniak.morenomodels.serviceserverless.services.PhotoStorageService;
import net.stepniak.morenomodels.serviceserverless.tables.records.ModelsRecord;
import net.stepniak.morenomodels.serviceserverless.tables.records.PhotosRecord;
import org.apache.commons.io.FilenameUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PhotosRepository {
    DSLContext dslContext;

    ModelsRepository modelsRepository;

    PhotoStorageService photoStorageService;

    @Inject
    public PhotosRepository(DSLContext dslContext,
                            ModelsRepository modelsRepository,
                            PhotoStorageService photoStorageService
    ) {
        this.dslContext = dslContext;
        this.modelsRepository = modelsRepository;
        this.photoStorageService = photoStorageService;
    }

    public PhotosRecord createPhoto(NewPhoto newPhoto) {
        return dslContext.transactionResult(configuration -> createPhoto(newPhoto, configuration)
                .orElseThrow(() -> new RuntimeException("Unexpected."))
        );
    }

    public Optional<PhotosRecord> deleteByPhotoSlug(String photoSlug) {
        return dslContext.transactionResult(configuration -> {
            Optional<PhotosRecord> model = findByPhotoSlug(photoSlug, configuration);
            if (model.isEmpty()) {
                return Optional.empty();
            }

            int result = DSL.using(configuration)
                    .deleteFrom(Tables.PHOTOS)
                    .where(Tables.PHOTOS.PHOTO_SLUG.eq(photoSlug))
                    .execute();
            if (result == 0) {
                return Optional.empty();
            }
            return model;
        });
    }

    public Optional<PhotosRecord> findByPhotoSlug(String photoSlug) {
        return dslContext.transactionResult(configuration -> findByPhotoSlug(photoSlug, configuration));
    }

    public PhotosPage list(
            String nextToken,
            Integer pageSize,
            PhotosFilters photosFilters
    ) {
        val baseData = dslContext.selectFrom(Tables.PHOTOS);

        Condition condition = DSL.noCondition();
        if (nextToken != null) {
             condition = condition.and(Tables.PHOTOS.PHOTO_ID.greaterThan(nextToken));
        }
        if (photosFilters.getShowArchived() != null && !photosFilters.getShowArchived()) {
            condition = condition.and(Tables.PHOTOS.ARCHIVED.eq(false));
        }
        if (photosFilters.getModelSlug() != null) {
            ModelsRecord model = modelsRepository.findByModelSlug(photosFilters.getModelSlug())
                    .orElseThrow(() -> new NotFoundException(String.format(
                            "Model of slug: [%s] does not exist and cannot be filtered by",
                            photosFilters.getModelSlug()
                    )));
            condition = condition.and(Tables.PHOTOS.MODEL_ID.likeIgnoreCase(model.getModelId()));
        }

        List<PhotosRecord> records = baseData.where(condition)
                .orderBy(Tables.PHOTOS.PHOTO_ID.asc())
                .limit(pageSize)
                .fetch()
                .stream()
                .collect(Collectors.toList());

        return PhotosPage.builder()
                .photos(records)
                .nextToken(records.size() == pageSize ? records.get(pageSize - 1).getPhotoId() : null)
                .build();
    }

    public Optional<PhotosRecord> update(String modelSlug, UpdatablePhoto updatablePhoto) {
        return dslContext.transactionResult(configuration ->
                        update(modelSlug, updatablePhoto, configuration)
                );
    }

    private Optional<PhotosRecord> createPhoto(NewPhoto newPhoto, Configuration configuration) {
        PhotosRecord photosRecord = new PhotosRecord();
        photosRecord.setPhotoId(UUID.randomUUID().toString());
        photosRecord.setPhotoSlug(newPhoto.getPhotoSlug());

        if (newPhoto.getModelSlug() != null) {
            ModelsRecord model = modelsRepository.findByModelSlug(newPhoto.getModelSlug())
                    .orElseThrow(() -> new NotFoundException(String.format(
                            "Model of slug: [%s] does not exist and cannot be assigned to a photo.",
                            newPhoto.getModelSlug()
                    )));
            photosRecord.setModelId(model.getModelId());
        }

        photosRecord.setArchived(false);
        photosRecord.setVersion(1);
        photosRecord.setCreated(OffsetDateTime.now());
        photosRecord.setUri(generateStoragePath(newPhoto.getPhotoSlug(), newPhoto.getFileName()));


        DSL.using(configuration)
                .insertInto(Tables.PHOTOS)
                .set(photosRecord)
                .execute();

        return findByPhotoSlug(photosRecord.getPhotoSlug(), configuration);
    }

    public String generateStoragePath(String photoSlug, String fileName) {
        // how to determine the extension?
        String extension = FilenameUtils.getExtension(fileName);
        return String.format("photos/%s.%s", photoSlug, extension);
    }

    private Optional<PhotosRecord> findByPhotoSlug(String photoSlug, Configuration configuration) {
        Result<PhotosRecord> record = DSL.using(configuration)
                .selectFrom(Tables.PHOTOS)
                .where(Tables.PHOTOS.PHOTO_SLUG.eq(photoSlug))
                .fetch();
        if (record.size() > 1) {
            throw new RuntimeException("Shouldn't happen.");
        }

        return record.stream()
                .findFirst();

    }

    private Optional<PhotosRecord> update(String photoSlug, UpdatablePhoto updatablePhoto, Configuration configuration) {
        Optional<PhotosRecord> photo = findByPhotoSlug(photoSlug, configuration);
        if (photo.isEmpty()) {
            throw new NotFoundException(String.format(
                    "Tried to update non-existing photo of slug: [%s]",
                    photoSlug
            ));
        }
        if (updatablePhoto.getVersion() != null &&
                !photo.get().getVersion().equals(updatablePhoto.getVersion())) {
            throw new DataExpiredException(String.format(
                    "Tried to updated model with version: [%d], but expected: [%d]",
                    photo.get().getVersion(), updatablePhoto.getVersion()
            ));
        }

        var update = DSL.using(configuration)
                .update(Tables.PHOTOS)
                .set(Tables.PHOTOS.VERSION, photo.get().getVersion() + 1);

        if (updatablePhoto.getArchived() != null) {
            update = update.set(Tables.PHOTOS.ARCHIVED, updatablePhoto.getArchived());
        }
        if (updatablePhoto.getWidth() != null) {
            update = update.set(Tables.PHOTOS.WIDTH, updatablePhoto.getWidth());
        }
        if (updatablePhoto.getHeight() != null) {
            update = update.set(Tables.PHOTOS.HEIGHT, updatablePhoto.getHeight());
        }
        if (updatablePhoto.getWidth() != null) {
            update = update.set(Tables.PHOTOS.URI, updatablePhoto.getUri());
        }

        update = update.set(Tables.PHOTOS.UPDATED, OffsetDateTime.now());


        update.where(Tables.PHOTOS.PHOTO_SLUG.eq(photoSlug))
                .execute();

        return findByPhotoSlug(photoSlug, configuration);
    }

    @Builder
    @Value
    public static class UpdatablePhoto {
        Integer width;
        Integer height;
        String uri;
        Integer version;
        Boolean archived;
    }

    @Builder
    @Value
    public static class PhotosPage {
        List<PhotosRecord> photos;
        String nextToken;
    }

    @Builder
    @Value
    public static class PhotosFilters {
        Boolean showArchived;
        String modelSlug;
    }
}
