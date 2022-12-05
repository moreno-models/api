package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.ModelsApiController
import net.stepniak.morenomodels.service.generated.model.*
import net.stepniak.morenomodels.service.springapi.entity.ModelEntity
import net.stepniak.morenomodels.service.springapi.repositories.ModelFilters
import net.stepniak.morenomodels.service.springapi.services.ModelsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class ModelsController(val modelsService: ModelsService) : ModelsApiController() {
    override fun listModels(
        @RequestParam("delete", required = false) nextToken: String?,
        @RequestParam("pageSize", required = false) pageSize: Int?,
        @RequestParam("showArchived", required = false) showArchived: Boolean?,
        @RequestParam("givenName", required = false)  givenName: String?
    ): ResponseEntity<Models> {
        val page = modelsService.listModels(
            nextToken,
            pageSize ?: 30,
            ModelFilters(showArchived ?: false, givenName)
        )
        return ResponseEntity.ok(
            Models(
                metadata = PaginationMetadata(
                    nextToken = page.nextToken
                ),
                items = page.models.map(::toApiModel)
            )
        )
    }

    override fun getModel(@PathVariable("modelSlug") modelSlug: String): ResponseEntity<Model> {
        val model = modelsService.getModel(modelSlug)

        return if (model != null) {
            ResponseEntity.ok(toApiModel(model))
        } else {
            ResponseEntity.notFound().build();
        }
    }

    override fun archiveModel(
        @PathVariable("modelSlug") modelSlug: String,
        @RequestParam("delete", required = false) delete: Boolean?
    ): ResponseEntity<Unit> {
        modelsService.archiveModel(modelSlug, delete ?: false)
        return ResponseEntity.ok(Unit)
    }

    override fun createModel(@RequestBody newModel: NewModel): ResponseEntity<Model> {
        return ResponseEntity.ok(
            toApiModel(modelsService.createModel(newModel))
        )
    }

    override fun updateModel(
        @PathVariable("modelSlug") modelSlug: String,
        @RequestBody updatableModel: UpdatableModel
    ): ResponseEntity<Model> {
        return ResponseEntity.ok(
            toApiModel(modelsService.updateModel(modelSlug, updatableModel))
        )
    }

    fun toApiModel(m: ModelEntity): Model = Model(
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
    )
}