package com.a1exymoroz.mergefruit

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a1exymoroz.mergefruit.di.AppContainer
import com.a1exymoroz.mergefruit.nav.MergeFruitNavGraph
import com.a1exymoroz.mergefruit.ui.theme.LocalGameTheme
import com.a1exymoroz.mergefruit.ui.theme.MergeFruitTheme
import kotlinx.coroutines.flow.StateFlow

/** Compose root, mirrors src/App.tsx (Provider + AuthProvider + BrowserRouter). */
@Composable
fun MergeFruitApp(
    container: AppContainer,
    deepLinkUri: StateFlow<Uri?>,
    onDeepLinkConsumed: () -> Unit,
) {
    val gameTheme by container.themeFlow.collectAsStateWithLifecycle()
    MergeFruitTheme {
        CompositionLocalProvider(LocalGameTheme provides gameTheme) {
            MergeFruitNavGraph(
                container = container,
                onSetTheme = container::setTheme,
                deepLinkUri = deepLinkUri,
                onDeepLinkConsumed = onDeepLinkConsumed,
            )
        }
    }
}
