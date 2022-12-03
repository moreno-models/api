package net.stepniak.morenmodels.api

import net.stepniak.morenomodels.api.DefaultApi
import okhttp3.OkHttpClient

open class BaseIntegrationTest {
    protected val api = DefaultApi("http://localhost:8080")
    protected val httpClient = OkHttpClient()
}