package net.stepniak.morenomodels.service.springapi.services

import net.stepniak.morenomodels.service.generated.model.NewPhoto
import net.stepniak.morenomodels.service.springapi.entity.Generator
import net.stepniak.morenomodels.service.springapi.entity.PhotoEntity
import net.stepniak.morenomodels.service.springapi.exceptions.NotFoundException
import net.stepniak.morenomodels.service.springapi.repositories.PhotoFilters
import net.stepniak.morenomodels.service.springapi.repositories.PhotosPage
import net.stepniak.morenomodels.service.springapi.repositories.PhotosRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PhotosService(private val photosRepository: PhotosRepository, private val generator: Generator) {
    fun listPhotos(nextToken: String?, pageSize: Int, filters: PhotoFilters): PhotosPage {
        return photosRepository.list(nextToken, pageSize, filters)
    }

    fun getPhoto(photoSlug: String): PhotoEntity? {
        return photosRepository.findByPhotoSlug(photoSlug)
    }

    @Transactional
    fun archivePhoto(photoSlug: String, delete: Boolean) {
        if (delete) {
            val deleted = photosRepository.deleteByPhotoSlug(photoSlug)
            if (deleted == 0L) {
                throw NotFoundException()
            }
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
        photo.photoSlug = newPhoto.modelSlug
        photo.version = 1
        photo.archived = false

        photosRepository.save(photo)

        return photo
    }
}