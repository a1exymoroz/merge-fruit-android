package com.a1exymoroz.mergefruit.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.a1exymoroz.mergefruit.R

/** Mirrors src/i18n/translateError.ts: maps the backend's fixed English error strings to localized copy. */
@Composable
fun translatedErrorMessage(raw: String?): String? {
    if (raw == null) return null
    val resId = when (raw) {
        "Authentication failed" -> R.string.error_authentication_failed
        "Email verification failed" -> R.string.error_email_verification_failed
        "Failed to fetch leaderboard" -> R.string.error_failed_to_fetch_leaderboard
        "Failed to load scores" -> R.string.error_failed_to_load_scores
        "Failed to submit score" -> R.string.error_failed_to_submit_score
        "Session expired. Please sign in again." -> R.string.error_session_expired
        "Too many requests. Please wait a moment." -> R.string.error_too_many_requests
        "Login failed" -> R.string.error_login_failed
        "Sign up failed" -> R.string.error_sign_up_failed
        "Verification failed" -> R.string.error_verification_failed
        else -> null
    }
    return resId?.let { stringResource(it) } ?: raw
}
