package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.PhotosApiController
import net.stepniak.morenomodels.service.generated.model.*
import net.stepniak.morenomodels.service.springapi.entity.PhotoEntity
import net.stepniak.morenomodels.service.springapi.repositories.ModelFilters
import net.stepniak.morenomodels.service.springapi.repositories.PhotoFilters
import net.stepniak.morenomodels.service.springapi.services.PhotosService
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class PhotosController(private val photosService: PhotosService) : PhotosApiController() {
    override fun listPhotos(
        @RequestParam("delete", required = false) nextToken: String?,
        @RequestParam("pageSize", required = false) pageSize: Int?,
        @RequestParam("showArchived", required = false) showArchived: Boolean?,
        @RequestParam("modelSlug", required = false)  modelSlug: String?
    ): ResponseEntity<Photos> {
        val page = photosService.listPhotos(
            nextToken,
            pageSize ?: 30,
            PhotoFilters(showArchived ?: false, modelSlug)
        )

        return ResponseEntity.ok(
            Photos(
                metadata = PaginationMetadata(
                    nextToken = page.nextToken
                ),
                items = page.photos.map(::toApiModel)
            )
        )
    }

    override fun archivePhoto(
        @PathVariable("modelSlug") photoSlug: String,
        @RequestParam("delete", required = false) delete: Boolean?
    ): ResponseEntity<Unit> {
        photosService.archivePhoto(photoSlug, delete ?: false)
        return ResponseEntity.ok(Unit)
    }

    override fun getPhoto(@PathVariable("photoSlog") photoSlug: String): ResponseEntity<Photo> {
        val photo = photosService.getPhoto(photoSlug)
        return if (photo != null) {
            ResponseEntity.ok(toApiModel(photo))
        } else {
            ResponseEntity.notFound().build();
        }
    }
    override fun createPhoto(@RequestBody newPhoto: NewPhoto): ResponseEntity<CreatedPhoto> {
        val photo = photosService.createPhoto(newPhoto)
        return ResponseEntity.ok(
            CreatedPhoto(
                photoId = photo.photoId!!,
                photoSlug = photo.photoSlug!!,
                uploadUri = URI.create("/photos/${photo.photoSlug}/upload")
            )
        )
    }

    override fun uploadPhoto(photoSlug: String, body: Resource): ResponseEntity<Photo> {
        return ResponseEntity.notFound().build();
    }

    fun toApiModel(p: PhotoEntity): Photo = Photo(
        photoSlug = p.photoSlug!!,
        photoId = p.photoId!!,
        archived = p.archived!!,
        version = p.version!!,
        created = p.created!!,
        uri = p.uri,
        width = p.width,
        height = p.height,
        modelSlug = p.model?.modelSlug,
        updated = p.updated,
    )
}