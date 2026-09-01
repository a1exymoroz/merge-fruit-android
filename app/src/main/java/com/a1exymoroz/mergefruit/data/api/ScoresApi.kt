package com.a1exymoroz.mergefruit.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Mirrors src/services/leaderboardApi.ts. GET /api/scores returns either a
 * bare JSON array or a `{content: [...]}` paginated shape depending on
 * backend config, so it's fetched as a raw string here and disambiguated in
 * ScoresRepository (see normalizeLeaderboard in the original).
 */
interface ScoresApi {
    @GET("api/scores")
    suspend fun getScoresRaw(@Header("Authorization") authorization: String): Response<String>

    @POST("api/scores")
    suspend fun submitScore(
        @Header("Authorization") authorization: String,
        @Body body: SubmitScoreRequest,
    ): Response<SubmitScoreResponseDto>
}
