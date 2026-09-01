package com.a1exymoroz.mergefruit.data.scores

import com.a1exymoroz.mergefruit.data.api.ApiException
import com.a1exymoroz.mergefruit.data.api.LeaderboardEntryDto
import com.a1exymoroz.mergefruit.data.api.PaginatedScoresDto
import com.a1exymoroz.mergefruit.data.api.ScoresApi
import com.a1exymoroz.mergefruit.data.api.SubmitScoreRequest
import com.a1exymoroz.mergefruit.data.api.SubmitScoreResponseDto
import com.a1exymoroz.mergefruit.data.auth.AuthStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private const val SESSION_EXPIRED = "Session expired. Please sign in again."

/** Mirrors src/services/leaderboardApi.ts. */
class ScoresRepository(
    private val scoresApi: ScoresApi,
    private val authStorage: AuthStorage,
    private val moshi: Moshi,
) {
    suspend fun getLeaderboard(): List<LeaderboardEntryDto> {
        val auth = authStorage.authHeader() ?: throw ApiException(SESSION_EXPIRED, 401)
        val response = scoresApi.getScoresRaw(auth)
        if (response.code() == 401) throw ApiException(SESSION_EXPIRED, 401)
        if (!response.isSuccessful) throw ApiException("Failed to fetch leaderboard", response.code())
        val raw = response.body() ?: return emptyList()
        return normalizeLeaderboard(raw)
    }

    suspend fun submitScore(score: Int): SubmitScoreResponseDto {
        val auth = authStorage.authHeader() ?: throw ApiException(SESSION_EXPIRED, 401)
        val response = scoresApi.submitScore(auth, SubmitScoreRequest(score))
        when (response.code()) {
            401 -> throw ApiException(SESSION_EXPIRED, 401)
            429 -> throw ApiException("Too many requests. Please wait a moment.", 429)
        }
        if (!response.isSuccessful) throw ApiException("Failed to submit score", response.code())
        return response.body() ?: throw ApiException("Failed to submit score", response.code())
    }

    fun highScoreFrom(entries: List<LeaderboardEntryDto>): Long = entries.firstOrNull()?.score ?: 0L

    /** API returns either a bare array or `{content: [...]}`; disambiguated by the first non-whitespace char. */
    private fun normalizeLeaderboard(raw: String): List<LeaderboardEntryDto> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return emptyList()

        return if (trimmed.startsWith("[")) {
            val listType = Types.newParameterizedType(List::class.java, LeaderboardEntryDto::class.java)
            moshi.adapter<List<LeaderboardEntryDto>>(listType).fromJson(trimmed) ?: emptyList()
        } else {
            moshi.adapter(PaginatedScoresDto::class.java).fromJson(trimmed)?.content ?: emptyList()
        }
    }
}
