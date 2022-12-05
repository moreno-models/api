package net.stepniak.morenomodels.service.springapi.repositories

import net.stepniak.morenomodels.service.springapi.entity.ModelEntity
import net.stepniak.morenomodels.service.springapi.entity.PhotoEntity
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Predicate

interface CustomPhotosRepository {
    fun list(nextToken: String?, pageSize: Int, filters: PhotoFilters): PhotosPage
}

interface PhotosRepository : JpaRepository<PhotoEntity, String>, CustomPhotosRepository {
    fun findByPhotoSlug(photoSlug: String): PhotoEntity?
    fun deleteByPhotoSlug(photoSlug: String): Long
}

class CustomPhotosRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) :
    CustomPhotosRepository {

    override fun list(nextToken: String?, pageSize: Int, filters: PhotoFilters): PhotosPage {
        val builder = entityManager.criteriaBuilder
        val query = builder.createQuery(PhotoEntity::class.java)
        val entity = query.from(PhotoEntity::class.java)

        val where = mutableListOf<Predicate>()

        if (nextToken != null)
            where.add(builder.greaterThan(entity.get("photoId"), nextToken))
        if (!filters.showArchived)
            where.add(builder.equal(entity.get<Boolean>("archived"), false))
        if (filters.modelSlug != null) {
            where.add(builder.equal(entity.get<ModelEntity>("model").get<String>("modelSlug"), filters.modelSlug))
        }

        query
            .where(*where.toTypedArray())
            .orderBy(builder.asc(entity.get<String>("photoId")))

        val results = entityManager.createQuery(query)
            .setMaxResults(pageSize)
            .resultList
        return PhotosPage(
            photos = results,
            nextToken = if (results.size == pageSize) results.lastOrNull()?.photoId else null
        )
    }

}

data class PhotosPage(val photos: List<PhotoEntity>, val nextToken: String? = null)

data class PhotoFilters(val showArchived: Boolean, val modelSlug: String? = null)