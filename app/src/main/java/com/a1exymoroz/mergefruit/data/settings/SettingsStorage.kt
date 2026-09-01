package com.a1exymoroz.mergefruit.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a1exymoroz.mergefruit.ui.theme.GameThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "merge_fruit_settings")

/** Persists user preferences that aren't tied to an account. Mirrors the DataStore pattern in [com.a1exymoroz.mergefruit.data.auth.AuthStorage]. */
class SettingsStorage(private val context: Context) {

    private val themeKey = stringPreferencesKey("game_theme")

    /** Emits the stored skin, defaulting to [GameThemeOption.CLASSIC] when unset or unrecognized. */
    val theme: Flow<GameThemeOption> = context.settingsDataStore.data.map { prefs ->
        runCatching { GameThemeOption.valueOf(prefs[themeKey] ?: "") }.getOrDefault(GameThemeOption.CLASSIC)
    }

    suspend fun setTheme(option: GameThemeOption) {
        context.settingsDataStore.edit { prefs -> prefs[themeKey] = option.name }
    }
}
