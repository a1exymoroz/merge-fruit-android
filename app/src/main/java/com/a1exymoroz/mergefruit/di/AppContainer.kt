package com.a1exymoroz.mergefruit.di

import android.content.Context
import com.a1exymoroz.mergefruit.data.api.ApiConfig
import com.a1exymoroz.mergefruit.data.api.AuthApi
import com.a1exymoroz.mergefruit.data.api.ColdStartRepository
import com.a1exymoroz.mergefruit.data.api.HealthApi
import com.a1exymoroz.mergefruit.data.api.ScoresApi
import com.a1exymoroz.mergefruit.data.auth.AuthRepository
import com.a1exymoroz.mergefruit.data.auth.AuthStorage
import com.a1exymoroz.mergefruit.data.scores.ScoresRepository
import com.a1exymoroz.mergefruit.data.settings.SettingsStorage
import com.a1exymoroz.mergefruit.ui.theme.GameThemeOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Small manual dependency container — the app is intentionally small enough
 * that Hilt/Koin would add setup overhead without real benefit. One instance
 * lives for the process lifetime, created in MergeFruitApplication.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Lives for the process lifetime; backs persistence writes and the initial auth load. */
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val authApi: AuthApi by lazy { ApiConfig.retrofit.create(AuthApi::class.java) }
    private val scoresApi: ScoresApi by lazy { ApiConfig.retrofit.create(ScoresApi::class.java) }
    private val healthApi: HealthApi by lazy { ApiConfig.retrofit.create(HealthApi::class.java) }

    val authStorage: AuthStorage by lazy { AuthStorage(appContext, ApiConfig.moshi) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApi, authStorage, ApiConfig.moshi, appScope)
    }

    val scoresRepository: ScoresRepository by lazy {
        ScoresRepository(scoresApi, authStorage, ApiConfig.moshi)
    }

    val coldStartRepository: ColdStartRepository by lazy {
        ColdStartRepository(healthApi, appScope)
    }

    private val settingsStorage: SettingsStorage by lazy { SettingsStorage(appContext) }

    /** The persisted skin, hot for the process lifetime so the UI can read it synchronously at startup. */
    val themeFlow: StateFlow<GameThemeOption> =
        settingsStorage.theme.stateIn(appScope, SharingStarted.Eagerly, GameThemeOption.CLASSIC)

    fun setTheme(option: GameThemeOption) {
        appScope.launch { settingsStorage.setTheme(option) }
    }
}
