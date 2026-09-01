package com.a1exymoroz.mergefruit.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "merge_fruit_auth")

/** Mirrors StoredAuth in the web's src/utils/authStorage.ts, persisted via DataStore instead of localStorage. */
@JsonClass(generateAdapter = true)
data class StoredAuth(
    val accessToken: String,
    val expiresAt: Long,
    val email: String,
    val displayName: String,
    val role: String,
    val emailVerified: Boolean,
    val verificationToken: String? = null,
)

class AuthStorage(private val context: Context, private val moshi: Moshi) {

    private val authKey = stringPreferencesKey("auth_json")
    private val adapter = moshi.adapter(StoredAuth::class.java)

    /** Emits the stored auth, or null if absent/expired (auto-clearing on expiry, like getStoredAuth()). */
    val storedAuth: Flow<StoredAuth?> = context.authDataStore.data.map { prefs ->
        val raw = prefs[authKey] ?: return@map null
        val auth = runCatching { adapter.fromJson(raw) }.getOrNull() ?: return@map null
        if (System.currentTimeMillis() >= auth.expiresAt) null else auth
    }

    suspend fun getStoredAuth(): StoredAuth? = storedAuth.first()

    suspend fun setStoredAuth(auth: StoredAuth) {
        context.authDataStore.edit { prefs -> prefs[authKey] = adapter.toJson(auth) }
    }

    suspend fun clearStoredAuth() {
        context.authDataStore.edit { prefs -> prefs.remove(authKey) }
    }

    suspend fun authHeader(): String? = getStoredAuth()?.let { "Bearer ${it.accessToken}" }
}
