package com.a1exymoroz.mergefruit.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.data.api.LeaderboardEntryDto
import com.a1exymoroz.mergefruit.ui.common.translatedErrorMessage
import com.a1exymoroz.mergefruit.ui.leaderboard.SubmitScoreUiState

/** Mirrors src/components/ui/GameOverOverlay.tsx. */
@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Long,
    isGuest: Boolean,
    displayName: String?,
    submitState: SubmitScoreUiState,
    leaderboardEntries: List<LeaderboardEntryDto>,
    onSubmitScore: () -> Unit,
    onPlayAgain: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val isNewHighScore = score > highScore && score > 0

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
        contentAlignment = Alignment.Center,
    ) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.width(320.dp).padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.game_over_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.game_final_score, "%,d".format(score)), modifier = Modifier.padding(top = 8.dp))
                if (isNewHighScore) {
                    Text(stringResource(R.string.game_new_high_score), modifier = Modifier.padding(top = 4.dp))
                }

                when {
                    isGuest -> {
                        Text(stringResource(R.string.game_log_in_to_save_prompt), modifier = Modifier.padding(top = 16.dp))
                        Button(onClick = onLoginClick, modifier = Modifier.padding(top = 8.dp)) {
                            Text(stringResource(R.string.auth_log_in))
                        }
                    }
                    !submitState.submitted -> {
                        Text(
                            stringResource(R.string.game_save_score_prompt, displayName ?: ""),
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Button(
                            onClick = onSubmitScore,
                            enabled = !submitState.submitting && score > 0,
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text(stringResource(if (submitState.submitting) R.string.game_saving else R.string.game_save_score))
                        }
                        submitState.error?.let {
                            Text(
                                translatedErrorMessage(it) ?: it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    else -> {
                        val rank = submitState.rank
                        Text(
                            if (rank != null && rank <= 10) {
                                stringResource(R.string.game_ranked, rank)
                            } else {
                                stringResource(R.string.game_score_saved)
                            },
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Text(
                            stringResource(R.string.leaderboard_title),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        leaderboardEntries.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(entry.name)
                                Text("%,d".format(entry.score), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(onClick = onPlayAgain, modifier = Modifier.padding(top = 20.dp)) {
                    Text(stringResource(R.string.game_play_again))
                }
            }
        }
    }
}
