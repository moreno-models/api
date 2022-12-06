package net.stepniak.morenomodels.service.springapi.entity

import org.hibernate.annotations.NaturalId
import java.net.URI
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity(name = "Photo")
@Table(name = "photos")
open class PhotoEntity() {
    constructor(photoId: String, created: OffsetDateTime) : this() {
        this.photoId = photoId
        this.created = created
    }

    @NotBlank
    @Id
    @Column(nullable = false, updatable = false, length = 64)
    open var photoId: String? = null
        protected set

    @NotBlank
    @NaturalId
    @Column(nullable = false, unique = true, length = 100)
    open var photoSlug: String? = null

    @Column(nullable = false)
    @Min(1)
    open var version: Int? = null

    @Column(nullable = false)
    open var archived: Boolean? = null

    @Column(nullable = false)
    open var created: OffsetDateTime? = null
        protected set

    open var updated: OffsetDateTime? = null

    open var width: Int? = null
    open var height: Int? = null
    // Stored in relative.
    open var uri: String? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "model_id", nullable = true)
    open var model: ModelEntity? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoEntity

        if (photoId != other.photoId) return false

        return true
    }

    override fun hashCode(): Int {
        return photoId?.hashCode() ?: 0
    }
}