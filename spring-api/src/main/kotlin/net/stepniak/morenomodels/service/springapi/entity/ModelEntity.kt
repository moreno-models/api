package net.stepniak.morenomodels.service.springapi.entity

import net.stepniak.morenomodels.service.generated.model.EyeColor
import org.hibernate.annotations.NaturalId
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity(name = "Model")
@Table(name = "models")
open class ModelEntity() {
    constructor(modelId: String, created: OffsetDateTime) : this() {
        this.modelId = modelId
        this.created = created
    }

    @NotBlank
    @Id
    @Column(nullable = false, updatable = false, length = 64)
    open var modelId: String? = null
        protected set

    @NotBlank
    @NaturalId
    @Column(nullable = false, unique = true, length = 100)
    open var modelSlug: String? = null

    @Size(max = 64)
    @NotBlank
    @Column(nullable = false, length = 64)
    open var givenName: String? = null

    @Size(max = 64)
    @NotBlank
    @Column(nullable = false, length = 64)
    open var familyName: String? = null

    @Column(nullable = false)
    @Min(1)
    open var version: Int? = null

    @Column(nullable = false)
    open var archived: Boolean? = null

    @Column(nullable = false)
    open var created: OffsetDateTime? = null
        protected set

    open var eyeColor: EyeColor? = null

    @Max(220)
    @Min(100)
    open var height: Int? = null

    open var updated: OffsetDateTime? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelEntity

        if (modelId != other.modelId) return false

        return true
    }

    override fun hashCode(): Int {
        return modelId?.hashCode() ?: 0
    }
}
