package net.stepniak.morenmodels.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.net.URI

class URIAdapter {
    @ToJson
    fun toJson(value: URI): String {
        return value.toString()
    }

    @FromJson
    fun fromJson(value: String): URI {
        return URI.create(value)
    }
}