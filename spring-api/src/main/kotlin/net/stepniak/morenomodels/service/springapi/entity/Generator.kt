package net.stepniak.morenomodels.service.springapi.entity

import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class Generator {
    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    fun now(): OffsetDateTime {
        return OffsetDateTime.now();
    }
}