package net.stepniak.morenomodels.serviceserverless;

import net.stepniak.morenomodels.serviceserverless.generated.ModelsApi;
import net.stepniak.morenomodels.serviceserverless.generated.model.Model;
import net.stepniak.morenomodels.serviceserverless.generated.model.Models;
import net.stepniak.morenomodels.serviceserverless.generated.model.NewModel;
import net.stepniak.morenomodels.serviceserverless.generated.model.UpdatableModel;
import net.stepniak.morenomodels.serviceserverless.repository.ModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

public class ModelsResource implements ModelsApi {
    private final Logger log = LoggerFactory.getLogger(ModelsResource.class);
    @Inject
    ModelRepository modelRepository;

    @Override
    public void archiveModel(String modelSlug, Boolean delete) {

    }

    @Override
    public Model createModel(NewModel newModel) {
        return null;
    }

    @Override
    public Model getModel(String modelSlug) {
        return null;
    }

    @Override
//    @Transactional
    public Models listModels(String nextToken, Integer pageSize, Boolean showArchived, String givenName) {
        List<Model> models = modelRepository.listAllModels()
                .stream().map(m ->
                        Model.builder().modelId(m.getModelId()).build()
                )
                .collect(Collectors.toList());

        return Models.builder().items(models).build();
    }

    @Override
    public Model updateModel(String modelSlug, UpdatableModel updatableModel) {
        return null;
    }
}
