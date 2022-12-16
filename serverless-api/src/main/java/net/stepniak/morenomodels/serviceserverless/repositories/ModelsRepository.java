package net.stepniak.morenomodels.serviceserverless.repositories;

import lombok.Builder;
import lombok.Value;
import lombok.val;
import net.stepniak.morenomodels.serviceserverless.Tables;
import net.stepniak.morenomodels.serviceserverless.exceptions.DataExpiredException;
import net.stepniak.morenomodels.serviceserverless.exceptions.NotFoundException;
import net.stepniak.morenomodels.serviceserverless.generated.model.EyeColor;
import net.stepniak.morenomodels.serviceserverless.generated.model.Model;
import net.stepniak.morenomodels.serviceserverless.generated.model.NewModel;
import net.stepniak.morenomodels.serviceserverless.generated.model.UpdatableModel;
import net.stepniak.morenomodels.serviceserverless.tables.records.ModelsRecord;
import net.stepniak.morenomodels.serviceserverless.tables.records.PhotosRecord;
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
public class ModelsRepository {
    DSLContext dslContext;

    @Inject
    public ModelsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public ModelsRecord createModel(NewModel newModel) {
        return dslContext.transactionResult(configuration -> createModel(newModel, configuration)
                .orElseThrow(() -> new RuntimeException("Unexpected."))
        );
    }

    public Optional<ModelsRecord> deleteByModelSlug(String modelSlug) {
        return dslContext.transactionResult(configuration -> {
            Optional<ModelsRecord> model = findByModelSlug(modelSlug, configuration);
            if (model.isEmpty()) {
                return Optional.empty();
            }

            int result = DSL.using(configuration)
                    .deleteFrom(Tables.MODELS)
                    .where(Tables.MODELS.MODEL_SLUG.eq(modelSlug))
                    .execute();
            if (result == 0) {
                return Optional.empty();
            }
            return model;
        });
    }



    public Optional<ModelsRecord> findByModelSlug(String modelSlug) {
        return dslContext.transactionResult(configuration -> findByModelSlug(modelSlug, configuration));
    }

    public ModelsPage list(
            String nextToken,
            Integer pageSize,
            ModelsFilters modelsFilters
    ) {
        val baseData = dslContext.selectFrom(Tables.MODELS);

        Condition condition = DSL.noCondition();
        if (nextToken != null) {
             condition = condition.and(Tables.MODELS.MODEL_ID.greaterThan(nextToken));
        }
        if (modelsFilters.showArchived != null && !modelsFilters.showArchived) {
            condition = condition.and(Tables.MODELS.ARCHIVED.eq(false));
        }
        if (modelsFilters.givenName != null) {
            condition = condition.and(Tables.MODELS.GIVEN_NAME.likeIgnoreCase(modelsFilters.givenName));
        }

        List<ModelsRecord> records = baseData.where(condition)
                .orderBy(Tables.MODELS.MODEL_ID.asc())
                .limit(pageSize)
                .fetch()
                .stream()
                .collect(Collectors.toList());

        return ModelsPage.builder()
                .models(records)
                .nextToken(records.size() == pageSize ? records.get(pageSize - 1).getModelId() : null)
                .build();
    }

    public Optional<ModelsRecord> update(String modelSlug, UpdatableModel updatableModel) {
        return dslContext.transactionResult(configuration ->
                        update(modelSlug, updatableModel, configuration)
                );
    }

    private Optional<ModelsRecord> createModel(NewModel newModel, Configuration configuration) {
        ModelsRecord modelsRecord = new ModelsRecord();
        modelsRecord.setModelId(UUID.randomUUID().toString());
        modelsRecord.setModelSlug(newModel.getModelSlug());
        modelsRecord.setFamilyName(newModel.getFamilyName());
        modelsRecord.setGivenName(newModel.getGivenName());
        modelsRecord.setEyeColor(newModel.getEyeColor() != null ? newModel.getEyeColor().toString() : null);
        modelsRecord.setHeight(newModel.getHeight());
        modelsRecord.setArchived(false);
        modelsRecord.setVersion(1);
        modelsRecord.setCreated(OffsetDateTime.now());


        DSL.using(configuration)
                .insertInto(Tables.MODELS)
                .set(modelsRecord)
                .execute();

        return findByModelSlug(newModel.getModelSlug(), configuration);
    }

     Optional<ModelsRecord> findByModelSlug(String modelSlug, Configuration configuration) {
        Result<ModelsRecord> record = DSL.using(configuration)
                .selectFrom(Tables.MODELS)
                .where(Tables.MODELS.MODEL_SLUG.eq(modelSlug))
                .fetch();
        if (record.size() > 1) {
            throw new RuntimeException("Shouldn't happen.");
        }

        return record.stream()
                .findFirst();

    }

    private Optional<ModelsRecord> update(String modelSlug, UpdatableModel updatableModel, Configuration configuration) {
        Optional<ModelsRecord> model = findByModelSlug(modelSlug, configuration);
        if (model.isEmpty()) {
            throw new NotFoundException(String.format(
                    "Tried to update non-existing model of slug: [%s]",
                    modelSlug
            ));
        }
        if (updatableModel.getVersion() != null &&
                !model.get().getVersion().equals(updatableModel.getVersion())) {
            throw new DataExpiredException(String.format(
                    "Tried to updated model with version: [%d], but expected: [%d]",
                    model.get().getVersion(), updatableModel.getVersion()
            ));
        }

        var update = DSL.using(configuration)
                .update(Tables.MODELS)
                .set(Tables.MODELS.VERSION, model.get().getVersion() + 1);

        if (updatableModel.getGivenName() != null) {
            update = update.set(Tables.MODELS.GIVEN_NAME, updatableModel.getGivenName());
        }

        if (updatableModel.getFamilyName() != null) {
            update = update.set(Tables.MODELS.FAMILY_NAME, updatableModel.getFamilyName());
        }

        if (updatableModel.getArchived() != null) {
            update = update.set(Tables.MODELS.ARCHIVED, updatableModel.getArchived());
        }

        if (updatableModel.getHeight() != null) {
            update = update.set(Tables.MODELS.HEIGHT, updatableModel.getHeight());
        }

        update = update.set(Tables.MODELS.EYE_COLOR, updatableModel.getEyeColor() != null ?
                updatableModel.getEyeColor().toString() : null);
        update = update.set(Tables.MODELS.HEIGHT, updatableModel.getHeight());
        update = update.set(Tables.MODELS.UPDATED, OffsetDateTime.now());


        update.where(Tables.MODELS.MODEL_SLUG.eq(modelSlug))
                .execute();

        return findByModelSlug(modelSlug, configuration);
    }

    public Optional<ModelsRecord> findByModelId(String modelId) {
        return Optional.empty();
    }

    @Builder
    @Value
    public static class ModelsPage {
        List<ModelsRecord> models;
        String nextToken;
    }

    @Builder
    public static class ModelsFilters {
        Boolean showArchived;
        String givenName;
    }
}
