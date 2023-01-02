package net.stepniak.morenomodels.service.springapi.services

import net.stepniak.morenomodels.service.generated.model.NewPhoto
import net.stepniak.morenomodels.service.springapi.entity.Generator
import net.stepniak.morenomodels.service.springapi.entity.PhotoEntity
import net.stepniak.morenomodels.service.springapi.exceptions.NotFoundException
import net.stepniak.morenomodels.service.springapi.repositories.ModelsRepository
import net.stepniak.morenomodels.service.springapi.repositories.PhotoFilters
import net.stepniak.morenomodels.service.springapi.repositories.PhotosPage
import net.stepniak.morenomodels.service.springapi.repositories.PhotosRepository
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URI
import java.time.OffsetDateTime
import javax.imageio.ImageIO
import javax.transaction.Transactional

@Service
class PhotosService(
    private val photosRepository: PhotosRepository,
    private val modelsRepository: ModelsRepository,
    private val generator: Generator,
    @Value("\${moreno_models.photo_storage.path}") private val storagePath: String
) {
    fun listPhotos(nextToken: String?, pageSize: Int, filters: PhotoFilters): PhotosPage {
        return photosRepository.list(nextToken, pageSize, filters)
    }

    fun getPhoto(photoSlug: String): PhotoEntity? {
        return photosRepository.findByPhotoSlug(photoSlug)
    }

    @Transactional
    fun archivePhoto(photoSlug: String, delete: Boolean) {
        if (delete) {
            val photo = photosRepository.findByPhotoSlug(photoSlug) ?: throw NotFoundException()
            if (photo.uri != null) {
                File("$storagePath/${photo.uri}").delete()
            }
            photosRepository.delete(photo)
        } else {
            val model = getPhoto(photoSlug) ?: throw NotFoundException()
            model.archived = true
            photosRepository.save(model)
        }
    }

    @Transactional
    fun createPhoto(newPhoto: NewPhoto): PhotoEntity {
        val photo = PhotoEntity(
            photoId = generator.uuid(),
            created = generator.now(),
        )
        photo.photoSlug = newPhoto.photoSlug
        photo.version = 1
        photo.archived = false

        val format = FilenameUtils.getExtension(newPhoto.fileName)
        val imageBaseName = "${photo.photoSlug}.${format.lowercase()}"
        photo.uri = "$imageBaseName"
        photo.model = if (newPhoto.modelSlug != null)  {
            val model = modelsRepository.findByModelSlug(newPhoto.modelSlug) ?: throw NotFoundException()
            model
        } else null

        photosRepository.save(photo)

        return photo
    }

//    @Transactional
//    fun uploadPhoto(photoSlug: String, image: BufferedImage, format: String): PhotoEntity {
//        val photo = photosRepository.findByPhotoSlug(photoSlug) ?: throw NotFoundException()
//
//        val imageBaseName = "${photoSlug}.${format.lowercase()}"
//        val outputFile = File("${storagePath}/$imageBaseName")
//        ImageIO.write(image, format, outputFile)
//
//        photo.width = image.width
//        photo.height = image.height
//        photo.uri = "$imageBaseName"
//        photo.updated = OffsetDateTime.now()
//
//        photosRepository.save(photo)
//
//        return photo
//    }

    @Transactional
    fun uploadPhoto(photoSlug: String, image: InputStream): PhotoEntity {
        val photo = photosRepository.findByPhotoSlug(photoSlug) ?: throw NotFoundException()
        val outputFile = File("${storagePath}/${photo.uri!!}")
        image.use {input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        photo.updated = OffsetDateTime.now()

        photosRepository.save(photo)

        return photo
    }
}