package com.a1exymoroz.mergefruit.data.api

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Mirrors src/services/healthApi.ts + src/hooks/useColdStart.ts: pings the
 * backend once per app session (Render's free tier sleeps after inactivity)
 * and exposes whether it's still likely cold — used to show a "this may
 * take ~50s" notice on the auth screens.
 */
class ColdStartRepository(
    private val healthApi: HealthApi,
    private val appScope: CoroutineScope,
) {
    companion object {
        private const val COLD_START_THRESHOLD_MS = 10_000L
        private const val PING_TIMEOUT_MS = 50_000L
        private const val RETRY_DELAY_MS = 1_000L
    }

    private val _isColdStart = MutableStateFlow(false)
    val isColdStart: StateFlow<Boolean> = _isColdStart

    private var started = false

    fun ensureWarmUp() {
        if (started) return
        started = true

        appScope.launch {
            val thresholdJob = launch {
                delay(COLD_START_THRESHOLD_MS)
                _isColdStart.value = true
            }
            while (!pingHealth()) {
                delay(RETRY_DELAY_MS)
            }
            thresholdJob.cancel()
            _isColdStart.value = false
        }
    }

    private suspend fun pingHealth(): Boolean {
        return try {
            withTimeoutOrNull(PING_TIMEOUT_MS) { healthApi.health().isSuccessful } ?: false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }
}
