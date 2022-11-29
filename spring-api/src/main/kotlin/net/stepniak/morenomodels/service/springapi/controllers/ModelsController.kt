package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.ModelsApiController
import net.stepniak.morenomodels.service.generated.model.Models
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ModelsController : ModelsApiController() {
    override fun listModels(nextToken: String?, pageSize: Int?): ResponseEntity<Models> {
        return super.listModels(nextToken, pageSize)
    }

}