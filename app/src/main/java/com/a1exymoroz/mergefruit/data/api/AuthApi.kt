package com.a1exymoroz.mergefruit.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Mirrors src/services/authApi.ts. */
interface AuthApi {
    @POST("api/auth/signup")
    suspend fun signUp(@Body body: SignUpRequest): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponseDto>

    @GET("api/auth/verify")
    suspend fun verifyEmail(@Query("token") token: String, @Query("code") code: String): Response<Void>
}
