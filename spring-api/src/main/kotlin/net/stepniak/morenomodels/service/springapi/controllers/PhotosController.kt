package net.stepniak.morenomodels.service.springapi.controllers

import net.stepniak.morenomodels.service.generated.PhotosApi
import net.stepniak.morenomodels.service.generated.model.*
import net.stepniak.morenomodels.service.springapi.entity.PhotoEntity
import net.stepniak.morenomodels.service.springapi.exceptions.NotImageException
import net.stepniak.morenomodels.service.springapi.repositories.PhotoFilters
import net.stepniak.morenomodels.service.springapi.services.PhotosService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.imageio.ImageIO

@RestController
class PhotosController(
    private val photosService: PhotosService,
    @Value("\${moreno_models.server_url}") private val serverOrigin: String
) : PhotosApi {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    override fun listPhotos(
        @RequestParam("nextToken", required = false) nextToken: String?,
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
        @PathVariable("photoSlug") photoSlug: String,
        @RequestParam("delete", required = false) delete: Boolean?
    ): ResponseEntity<Unit> {
        photosService.archivePhoto(photoSlug, delete ?: false)
        return ResponseEntity.ok().build();
    }

    override fun getPhoto(@PathVariable("photoSlug") photoSlug: String): ResponseEntity<Photo> {
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
                uploadUri = "${serverOrigin}/photos/${photo.photoSlug}/upload"
            )
        )
    }

//    override fun uploadPhoto(@PathVariable("photoSlug") photoSlug: String, @RequestBody body: Resource): ResponseEntity<Photo> {
//        try {
//            ImageIO.createImageInputStream(body.inputStream).use {
//                val readers = ImageIO.getImageReaders(it)
//                val imageReader = readers.next()
//                imageReader.input = it
//                val format = imageReader.formatName
//                val image = imageReader.read(0)
//
//                val photo = photosService.uploadPhoto(photoSlug, image, format)
//                return ResponseEntity.ok(toApiModel(photo))
//            }
//        } catch (e: Exception) {
//            LOG.warn("Tried to upload something which is not an image", e)
//            throw NotImageException()
//        }
//    }

    override fun uploadPhoto(@PathVariable("photoSlug") photoSlug: String, @RequestBody body: Resource): ResponseEntity<Photo> {
        try {
            val photo = photosService.uploadPhoto(photoSlug, body.inputStream)
            return ResponseEntity.ok(toApiModel(photo))
        } catch (e: Exception) {
            LOG.warn("Tried to upload something which is not an image", e)
            throw NotImageException()
        }
    }

    fun toApiModel(p: PhotoEntity): Photo = Photo(
        photoSlug = p.photoSlug!!,
        photoId = p.photoId!!,
        archived = p.archived!!,
        version = p.version!!,
        created = p.created!!,
        uri = if (p.uri != null) "$serverOrigin/content/${p.uri}" else null,
        width = p.width,
        height = p.height,
        modelSlug = p.model?.modelSlug,
        updated = p.updated,
    )
}