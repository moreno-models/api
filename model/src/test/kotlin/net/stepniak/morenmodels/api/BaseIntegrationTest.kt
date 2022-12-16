package net.stepniak.morenmodels.api

import net.stepniak.morenomodels.api.DefaultApi
import net.stepniak.morenomodels.api.infrastructure.Serializer
import okhttp3.OkHttpClient
import java.time.Duration

open class BaseIntegrationTest {
    protected val httpClient = OkHttpClient.Builder()
        // https://aws.amazon.com/premiumsupport/knowledge-center/api-gateway-504-errors/
        // Account for lambda cold-start.
        // AWS API Gateway REST API can wait max 29 secs max and can't be increased.
        .readTimeout(Duration.ofSeconds(29))
        .build()

    protected val api = DefaultApi(
        "http://localhost:8080",
        httpClient
    )
}