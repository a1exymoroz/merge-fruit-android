package com.a1exymoroz.mergefruit.data.auth

import com.a1exymoroz.mergefruit.data.api.ApiErrorDto
import com.a1exymoroz.mergefruit.data.api.ApiException
import com.a1exymoroz.mergefruit.data.api.AuthApi
import com.a1exymoroz.mergefruit.data.api.AuthResponseDto
import com.a1exymoroz.mergefruit.data.api.LoginRequest
import com.a1exymoroz.mergefruit.data.api.SignUpRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

data class AuthUiState(
    val user: StoredAuth? = null,
    /** Session-scoped only (resets on process death), mirroring the web's sessionStorage guest flag. */
    val isGuest: Boolean = false,
    val isLoading: Boolean = true,
) {
    val isAuthenticated: Boolean get() = user != null
    val isEmailVerified: Boolean get() = user?.emailVerified ?: false
}

/**
 * Mirrors src/contexts/AuthContext.tsx. [appScope] is an application-lifetime
 * coroutine scope (see AppContainer) used for persistence writes and the
 * initial load, which aren't tied to any single screen's lifecycle.
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val authStorage: AuthStorage,
    private val moshi: Moshi,
    private val appScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    init {
        appScope.launch {
            val stored = authStorage.getStoredAuth()
            _state.value = _state.value.copy(user = stored, isLoading = false)
        }
    }

    suspend fun login(email: String, password: String): StoredAuth {
        val dto = parseAuthResponse(authApi.login(LoginRequest(email, password)), "Login failed")
        return persist(dto)
    }

    suspend fun signUp(email: String, password: String, displayName: String): StoredAuth {
        val dto = parseAuthResponse(authApi.signUp(SignUpRequest(email, password, displayName)), "Sign up failed")
        return persist(dto)
    }

    suspend fun verifyEmail(token: String, code: String) {
        val response = authApi.verifyEmail(token, code)
        if (!response.isSuccessful) {
            throw errorFrom(response.errorBody(), response.code(), "Email verification failed")
        }
    }

    fun markEmailVerified() {
        val current = _state.value.user ?: return
        val updated = current.copy(emailVerified = true, verificationToken = null)
        _state.value = _state.value.copy(user = updated)
        appScope.launch { authStorage.setStoredAuth(updated) }
    }

    fun continueAsGuest() {
        _state.value = _state.value.copy(isGuest = true)
    }

    fun logout() {
        _state.value = AuthUiState(user = null, isGuest = false, isLoading = false)
        appScope.launch { authStorage.clearStoredAuth() }
    }

    private suspend fun persist(dto: AuthResponseDto): StoredAuth {
        val stored = StoredAuth(
            accessToken = dto.accessToken,
            expiresAt = System.currentTimeMillis() + dto.expiresInMs,
            email = dto.email,
            displayName = dto.displayName,
            role = dto.role,
            emailVerified = dto.emailVerified ?: (dto.verificationToken == null),
            verificationToken = dto.verificationToken,
        )
        authStorage.setStoredAuth(stored)
        _state.value = _state.value.copy(user = stored, isGuest = false)
        return stored
    }

    private fun parseAuthResponse(response: Response<AuthResponseDto>, defaultMessage: String): AuthResponseDto {
        if (response.isSuccessful) {
            return response.body() ?: throw ApiException(defaultMessage, response.code())
        }
        throw errorFrom(response.errorBody(), response.code(), defaultMessage)
    }

    private fun errorFrom(errorBody: ResponseBody?, status: Int, defaultMessage: String): ApiException {
        val dto = errorBody?.string()?.let { raw ->
            runCatching { moshi.adapter(ApiErrorDto::class.java).fromJson(raw) }.getOrNull()
        }
        val message = dto?.message ?: dto?.error ?: defaultMessage
        return ApiException(message, status, dto?.verificationToken)
    }
}
