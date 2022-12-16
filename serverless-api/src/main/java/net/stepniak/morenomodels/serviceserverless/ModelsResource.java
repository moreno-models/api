package net.stepniak.morenomodels.serviceserverless;

import net.stepniak.morenomodels.serviceserverless.exceptions.NotFoundException;
import net.stepniak.morenomodels.serviceserverless.generated.ModelsApi;
import net.stepniak.morenomodels.serviceserverless.generated.model.*;
import net.stepniak.morenomodels.serviceserverless.repositories.ModelsRepository;
import net.stepniak.morenomodels.serviceserverless.tables.records.ModelsRecord;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class ModelsResource implements ModelsApi {
    @Inject
    ModelsRepository modelsRepository;

    @Override
    public void archiveModel(String modelSlug, Boolean delete) {
        if (delete != null && delete) {
            modelsRepository.deleteByModelSlug(modelSlug)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Model with slug [%s] not found", modelSlug)
                    ));
        } else {
            modelsRepository.update(modelSlug, UpdatableModel.builder()
                    .archived(true)
                    .build()
            );
        }
    }

    @Override
    public Model createModel(NewModel newModel) {
        return toApiModel(modelsRepository.createModel(newModel));
    }

    @Override
    public Model getModel(String modelSlug) {
        return modelsRepository.findByModelSlug(modelSlug)
                .map(ModelsResource::toApiModel)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Model with slug [%s] not found", modelSlug)
                ));
    }

    @Override
    public Models listModels(String nextToken, Integer pageSize, Boolean showArchived, String givenName) {
        if (pageSize == null) {
            pageSize = 20;
        }
        ModelsRepository.ModelsPage page = modelsRepository.list(
                nextToken,
                pageSize,
                ModelsRepository.ModelsFilters.builder()
                        .showArchived(showArchived)
                        .givenName(givenName)
                        .build()
        );

        return Models.builder()
                .items(page.getModels().stream().map(ModelsResource::toApiModel).collect(Collectors.toList()))
                .metadata(PaginationMetadata.builder()
                        .nextToken(page.getNextToken())
                        .build()
                ).build();
    }

    @Override
    public Model updateModel(String modelSlug, UpdatableModel updatableModel) {
        return modelsRepository.update(modelSlug, updatableModel)
                .map(ModelsResource::toApiModel)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Model [%s] was deleted while being updated", modelSlug)
                ));
    }

    private static Model toApiModel(ModelsRecord r) {
        return Model.builder()
                .modelId(r.getModelId())
                .modelSlug(r.getModelSlug())
                .familyName(r.getFamilyName())
                .givenName(r.getGivenName())
                .archived(r.getArchived())
                .height(r.getHeight())
                .eyeColor(r.getEyeColor() != null ? EyeColor.fromValue(r.getEyeColor()) : null)
                .version(r.getVersion())
                .updated(r.getUpdated())
                .created(r.getCreated())
                .build();
    }
}
