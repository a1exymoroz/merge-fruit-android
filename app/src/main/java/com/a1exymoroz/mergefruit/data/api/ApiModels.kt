package com.a1exymoroz.mergefruit.data.api

import com.squareup.moshi.JsonClass

/** Mirrors the web's src/services/authApi.ts request/response shapes. */
@JsonClass(generateAdapter = true)
data class SignUpRequest(val email: String, val password: String, val displayName: String)

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String? = null,
    val expiresInMs: Long,
    val email: String,
    val displayName: String,
    val role: String,
    val emailVerified: Boolean? = null,
    val verificationToken: String? = null,
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    val message: String? = null,
    val error: String? = null,
    val verificationToken: String? = null,
)

/** Mirrors src/services/leaderboardApi.ts. */
@JsonClass(generateAdapter = true)
data class LeaderboardEntryDto(
    val id: Long? = null,
    val name: String,
    val score: Long,
    val timestamp: String,
)

@JsonClass(generateAdapter = true)
data class PaginatedScoresDto(val content: List<LeaderboardEntryDto>)

@JsonClass(generateAdapter = true)
data class SubmitScoreRequest(val score: Int)

@JsonClass(generateAdapter = true)
data class SubmitScoreResponseDto(
    val success: Boolean,
    val rank: Int,
    val leaderboard: List<LeaderboardEntryDto>,
)

/** Non-body error surfaced to the UI layer, mirrors AuthApiError in authApi.ts. */
class ApiException(
    message: String,
    val httpStatus: Int,
    @Suppress("unused") val verificationToken: String? = null,
) : Exception(message)
