package net.stepniak.morenomodels.service.springapi.services

import net.stepniak.morenomodels.service.springapi.entity.ModelEntity
import net.stepniak.morenomodels.service.springapi.exceptions.NotFoundException
import net.stepniak.morenomodels.service.springapi.repositories.ModelFilters
import net.stepniak.morenomodels.service.springapi.repositories.ModelsPage
import net.stepniak.morenomodels.service.springapi.repositories.ModelsRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ModelsService(private val modelsRepository: ModelsRepository) {
    fun listModels(nextToken: String?, pageSize: Int, filters: ModelFilters): ModelsPage {
        return modelsRepository.list(nextToken, pageSize, filters)
    }

    fun getModel(modelSlug: String): ModelEntity? {
        return modelsRepository.findByModelSlug(modelSlug)
    }

    @Transactional
    fun archiveModel(modelSlug: String, delete: Boolean) {
        if (delete) {
            modelsRepository.deleteByModelSlug(modelSlug)
        } else {
            val model = getModel(modelSlug) ?: throw NotFoundException()
            model.archived = true
            modelsRepository.save(model)
        }
    }
}