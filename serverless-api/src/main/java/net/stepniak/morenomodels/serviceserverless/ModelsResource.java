package net.stepniak.morenomodels.serviceserverless;

import net.stepniak.morenomodels.serviceserverless.generated.ModelsApi;
import net.stepniak.morenomodels.serviceserverless.generated.model.Model;
import net.stepniak.morenomodels.serviceserverless.generated.model.Models;
import net.stepniak.morenomodels.serviceserverless.generated.model.NewModel;
import net.stepniak.morenomodels.serviceserverless.generated.model.UpdatableModel;

import java.util.List;

public class ModelsResource implements ModelsApi {

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
    public Models listModels(String nextToken, Integer pageSize, Boolean showArchived, String givenName) {
        return Models.builder().items(List.of()).build();
    }

    @Override
    public Model updateModel(String modelSlug, UpdatableModel updatableModel) {
        return null;
    }
}
