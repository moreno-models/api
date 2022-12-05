package net.stepniak.morenomodels.service.springapi.repositories

import net.stepniak.morenomodels.service.springapi.entity.ModelEntity
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Predicate

interface CustomModelsRepository {
    fun list(nextToken: String?, pageSize: Int, filters: ModelFilters): ModelsPage
}

interface ModelsRepository : JpaRepository<ModelEntity, String>, CustomModelsRepository {
    fun findByModelSlug(modelSlug: String): ModelEntity?
    fun deleteByModelSlug(modelSlug: String): Long
}

class CustomModelsRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) :
    CustomModelsRepository {

    override fun list(nextToken: String?, pageSize: Int, filters: ModelFilters): ModelsPage {
        val builder = entityManager.criteriaBuilder
        val query = builder.createQuery(ModelEntity::class.java)
        val entity = query.from(ModelEntity::class.java)

        val where = mutableListOf<Predicate>()

        if (nextToken != null)
            where.add(builder.greaterThan(entity.get("modelId"), nextToken))
        if (!filters.showArchived)
            where.add(builder.equal(entity.get<Boolean>("archived"), false))
        if (filters.givenName != null)
            where.add(builder.like(entity.get("givenName"), filters.givenName))

        query
            .where(*where.toTypedArray())
            .orderBy(builder.asc(entity.get<String>("modelId")))

        val results = entityManager.createQuery(query)
            .setMaxResults(pageSize)
            .resultList
        return ModelsPage(
            models = results,
            nextToken = if (results.size == pageSize) results.lastOrNull()?.modelId else null
        )
    }

}

data class ModelsPage(val models: List<ModelEntity>, val nextToken: String? = null)

data class ModelFilters(val showArchived: Boolean, val givenName: String? = null)