package com.a1exymoroz.mergefruit.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a1exymoroz.mergefruit.data.api.LeaderboardEntryDto
import com.a1exymoroz.mergefruit.data.scores.ScoresRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val entries: List<LeaderboardEntryDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    /** Mirrors getHighScoreFromEntries: the list is server-sorted descending, so the first entry is the high score. */
    val highScore: Long get() = entries.firstOrNull()?.score ?: 0L
}

data class SubmitScoreUiState(
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val rank: Int? = null,
    val error: String? = null,
)

/** Mirrors src/store/scoresSlice.ts + selectors.ts + the submit flow in GameOverOverlay.tsx. */
class LeaderboardViewModel(private val scoresRepository: ScoresRepository) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state

    private val _submitState = MutableStateFlow(SubmitScoreUiState())
    val submitState: StateFlow<SubmitScoreUiState> = _submitState

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val entries = scoresRepository.getLeaderboard()
                _state.value = LeaderboardUiState(entries = entries, loading = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Failed to load scores")
            }
        }
    }

    fun submitScore(score: Int) {
        if (_submitState.value.submitting || score <= 0) return
        viewModelScope.launch {
            _submitState.value = SubmitScoreUiState(submitting = true)
            try {
                val result = scoresRepository.submitScore(score)
                _state.value = _state.value.copy(entries = result.leaderboard)
                _submitState.value = SubmitScoreUiState(submitted = true, rank = result.rank)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _submitState.value = SubmitScoreUiState(error = e.message ?: "Failed to submit score")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitScoreUiState()
    }

    fun updateEntries(entries: List<LeaderboardEntryDto>) {
        _state.value = _state.value.copy(entries = entries)
    }
}
