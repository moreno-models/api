package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.PhotosApiController
import net.stepniak.morenomodels.service.generated.model.Photos
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotosController : PhotosApiController() {
    override fun listPhotos(
        nextToken: String?,
        pageSize: Int?,
        showArchived: Boolean?,
        modelSlug: String?
    ): ResponseEntity<Photos> {
        return super.listPhotos(nextToken, pageSize, showArchived, modelSlug)
    }

}