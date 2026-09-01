package com.a1exymoroz.mergefruit.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a1exymoroz.mergefruit.game.GameViewModel
import com.a1exymoroz.mergefruit.ui.auth.AuthViewModel
import com.a1exymoroz.mergefruit.ui.leaderboard.LeaderboardViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(container.authRepository, container.coldStartRepository) as T
            modelClass.isAssignableFrom(LeaderboardViewModel::class.java) ->
                LeaderboardViewModel(container.scoresRepository) as T
            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
