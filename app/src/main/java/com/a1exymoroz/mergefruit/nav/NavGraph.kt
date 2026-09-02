package com.a1exymoroz.mergefruit.nav

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.di.AppContainer
import com.a1exymoroz.mergefruit.di.AppViewModelFactory
import com.a1exymoroz.mergefruit.game.GameViewModel
import com.a1exymoroz.mergefruit.ui.auth.AuthViewModel
import com.a1exymoroz.mergefruit.ui.auth.GuestGateScreen
import com.a1exymoroz.mergefruit.ui.auth.LoginScreen
import com.a1exymoroz.mergefruit.ui.auth.SignUpScreen
import com.a1exymoroz.mergefruit.ui.auth.VerifyEmailScreen
import com.a1exymoroz.mergefruit.ui.game.GameScreen
import com.a1exymoroz.mergefruit.ui.leaderboard.LeaderboardViewModel
import com.a1exymoroz.mergefruit.ui.theme.GameThemeOption
import com.a1exymoroz.mergefruit.ui.theme.appBackground
import kotlinx.coroutines.flow.StateFlow

private object Routes {
    const val ROOT = "root"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val VERIFY_PATTERN = "verify?token={token}&checkEmail={checkEmail}"

    fun verify(token: String?, checkEmail: Boolean): String =
        "verify?token=${Uri.encode(token ?: "")}&checkEmail=$checkEmail"
}

/**
 * Mirrors src/App.tsx's route table plus the gating in ProtectedRoute.tsx /
 * GuestRoute.tsx / GuestGateModal.tsx. "root" is the single always-present
 * destination whose content reacts to auth state directly (guest gate, the
 * game, or an inline verify prompt) instead of Navigation-Compose redirects,
 * since that state changes independently of user-driven navigation.
 */
@Composable
fun MergeFruitNavGraph(
    container: AppContainer,
    onSetTheme: (GameThemeOption) -> Unit,
    deepLinkUri: StateFlow<Uri?>,
    onDeepLinkConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val factory = remember { AppViewModelFactory(container) }
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val leaderboardViewModel: LeaderboardViewModel = viewModel(factory = factory)

    val authState by authViewModel.state.collectAsStateWithLifecycle()

    fun goHome() {
        navController.popBackStack(route = Routes.ROOT, inclusive = false)
    }

    fun goToLogin() {
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.ROOT) { inclusive = false }
            launchSingleTop = true
        }
    }

    // Verify-email deep link (mergefruit://verify?token=...) from the signup email.
    val deepLink by deepLinkUri.collectAsStateWithLifecycle()
    LaunchedEffect(deepLink) {
        val uri = deepLink ?: return@LaunchedEffect
        if (uri.host == "verify") {
            navController.navigate(Routes.verify(uri.getQueryParameter("token"), checkEmail = false)) {
                launchSingleTop = true
            }
        }
        onDeepLinkConsumed()
    }

    NavHost(navController = navController, startDestination = Routes.ROOT) {
        composable(Routes.ROOT) {
            when {
                authState.isLoading -> Box(Modifier.fillMaxSize().appBackground(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.common_loading), color = Color.White)
                }

                !authState.isAuthenticated && !authState.isGuest -> GuestGateScreen(
                    authViewModel = authViewModel,
                    onContinueAsGuest = { /* authState flips reactively; no navigation needed */ },
                    onNavigateToLogin = ::goToLogin,
                    onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) { launchSingleTop = true } },
                )

                authState.isAuthenticated && !authState.isEmailVerified -> VerifyEmailScreen(
                    token = authState.user?.verificationToken,
                    showCheckEmailMessage = false,
                    authViewModel = authViewModel,
                    onNavigateToLogin = { authViewModel.logout(); },
                    onVerified = { /* authState flips reactively */ },
                )

                else -> {
                    val gameViewModel: GameViewModel = viewModel(factory = factory)
                    GameScreen(
                        gameViewModel = gameViewModel,
                        authViewModel = authViewModel,
                        leaderboardViewModel = leaderboardViewModel,
                        onNavigateToLogin = ::goToLogin,
                        onSetTheme = onSetTheme,
                    )
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = ::goHome,
                onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) { launchSingleTop = true } },
                onNavigateToVerify = { token, checkEmail -> navController.navigate(Routes.verify(token, checkEmail)) },
            )
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = ::goHome,
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                onNavigateToVerify = { token, checkEmail -> navController.navigate(Routes.verify(token, checkEmail)) },
            )
        }

        composable(
            route = Routes.VERIFY_PATTERN,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType; defaultValue = "" },
                navArgument("checkEmail") { type = NavType.BoolType; defaultValue = false },
            ),
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")?.takeIf { it.isNotEmpty() }
            val checkEmail = backStackEntry.arguments?.getBoolean("checkEmail") ?: false
            VerifyEmailScreen(
                token = token,
                showCheckEmailMessage = checkEmail,
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.ROOT) { inclusive = false }; launchSingleTop = true } },
                onVerified = ::goHome,
            )
        }
    }
}
