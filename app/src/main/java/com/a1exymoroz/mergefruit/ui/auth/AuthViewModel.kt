package com.a1exymoroz.mergefruit.ui.auth

import androidx.lifecycle.ViewModel
import com.a1exymoroz.mergefruit.data.api.ColdStartRepository
import com.a1exymoroz.mergefruit.data.auth.AuthRepository
import com.a1exymoroz.mergefruit.data.auth.AuthUiState
import com.a1exymoroz.mergefruit.data.auth.StoredAuth
import kotlinx.coroutines.flow.StateFlow

/** Thin wrapper around AuthRepository — screens own their local form/submitting state and call these directly. */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val coldStartRepository: ColdStartRepository,
) : ViewModel() {

    val state: StateFlow<AuthUiState> = authRepository.state
    val isColdStart: StateFlow<Boolean> = coldStartRepository.isColdStart

    fun warmUpBackend() = coldStartRepository.ensureWarmUp()

    suspend fun login(email: String, password: String): StoredAuth = authRepository.login(email, password)

    suspend fun signUp(email: String, password: String, displayName: String): StoredAuth =
        authRepository.signUp(email, password, displayName)

    suspend fun verifyEmail(token: String, code: String) = authRepository.verifyEmail(token, code)

    fun markEmailVerified() = authRepository.markEmailVerified()

    fun continueAsGuest() = authRepository.continueAsGuest()

    fun logout() = authRepository.logout()
}
