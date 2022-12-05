package net.stepniak.morenomodels.service.springapi.services

import net.stepniak.morenomodels.service.generated.model.NewModel
import net.stepniak.morenomodels.service.generated.model.UpdatableModel
import net.stepniak.morenomodels.service.springapi.entity.ModelEntity
import net.stepniak.morenomodels.service.springapi.entity.Generator
import net.stepniak.morenomodels.service.springapi.exceptions.DataExpiredException
import net.stepniak.morenomodels.service.springapi.exceptions.NotFoundException
import net.stepniak.morenomodels.service.springapi.repositories.ModelFilters
import net.stepniak.morenomodels.service.springapi.repositories.ModelsPage
import net.stepniak.morenomodels.service.springapi.repositories.ModelsRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import javax.transaction.Transactional

@Service
class ModelsService(private val modelsRepository: ModelsRepository, private val generator: Generator) {
    fun listModels(nextToken: String?, pageSize: Int, filters: ModelFilters): ModelsPage {
        return modelsRepository.list(nextToken, pageSize, filters)
    }

    fun getModel(modelSlug: String): ModelEntity? {
        return modelsRepository.findByModelSlug(modelSlug)
    }

    @Transactional
    fun archiveModel(modelSlug: String, delete: Boolean) {
        if (delete) {
            val deleted = modelsRepository.deleteByModelSlug(modelSlug)
            if (deleted == 0L) {
                throw NotFoundException();
            }
        } else {
            val model = getModel(modelSlug) ?: throw NotFoundException()
            model.archived = true
            modelsRepository.save(model)
        }
    }

    @Transactional
    fun createModel(newModel: NewModel): ModelEntity {
        val model = ModelEntity(
            modelId = generator.uuid(),
            created = generator.now(),
        )
        model.modelSlug = newModel.modelSlug
        model.familyName = newModel.familyName
        model.givenName = newModel.givenName
        model.eyeColor = newModel.eyeColor
        model.height = newModel.height
        model.version = 1
        model.archived = false

        modelsRepository.save(model)

        return model
    }

    @Transactional
    fun updateModel(modelSlug: String, updatableModel: UpdatableModel): ModelEntity {
        val model = modelsRepository.findByModelSlug(modelSlug) ?: throw NotFoundException()
        if (model.version != updatableModel.version) {
            throw DataExpiredException()
        }

        if (updatableModel.familyName != null) {
            model.familyName = updatableModel.familyName
        }
        if (updatableModel.givenName != null) {
            model.givenName = updatableModel.givenName
        }
        if (updatableModel.familyName != null) {
            model.familyName = updatableModel.familyName
        }

        model.eyeColor = updatableModel.eyeColor
        model.height = updatableModel.height

        if (updatableModel.archived != null) {
            model.archived = updatableModel.archived
        }
        model.updated = generator.now()
        model.version = updatableModel.version + 1

        return model
    }
}