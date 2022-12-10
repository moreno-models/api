package net.stepniak.morenomodels.serviceserverless.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import net.stepniak.morenomodels.serviceserverless.entity.ModelEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ModelRepository implements PanacheRepositoryBase<ModelEntity, String> {
    public List<ModelEntity> listAllModels() {
        return this.listAll(Sort.ascending("modelId"));
    }
}
