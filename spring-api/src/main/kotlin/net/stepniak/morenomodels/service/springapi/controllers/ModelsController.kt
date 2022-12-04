package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.ModelsApiController
import net.stepniak.morenomodels.service.generated.model.Model
import net.stepniak.morenomodels.service.generated.model.Models
import net.stepniak.morenomodels.service.generated.model.PaginationMetadata
import net.stepniak.morenomodels.service.springapi.entity.ModelRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ModelsController(val modelRepository: ModelRepository) : ModelsApiController() {
    override fun listModels(nextToken: String?, pageSize: Int?, showArchived: Boolean?): ResponseEntity<Models> {
        return ResponseEntity.ok(Models(
            metadata = PaginationMetadata(),
            items = modelRepository.findAll().map { m -> Model(
                modelId = m.modelId!!,
                modelSlug = m.modelSlug!!,
                givenName = m.givenName!!,
                familyName = m.familyName!!,
                version = m.version!!,
                created = m.created!!,
                archived = m.archived!!,
                updated = m.updated,
                eyeColor = m.eyeColor,
                height = m.height,
            )}
        ))
    }
}