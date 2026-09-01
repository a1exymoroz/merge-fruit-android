package com.a1exymoroz.mergefruit.data.api

import retrofit2.Response
import retrofit2.http.GET

/** Mirrors src/services/healthApi.ts — used to warm up a sleeping Render free-tier backend. */
interface HealthApi {
    @GET("actuator/health")
    suspend fun health(): Response<Void>
}
