package com.a1exymoroz.mergefruit.ui.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.common.translatedErrorMessage
import com.a1exymoroz.mergefruit.ui.theme.TextSecondary

/** Mirrors src/components/ui/Leaderboard.tsx. */
@Composable
fun LeaderboardSection(state: LeaderboardUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.leaderboard_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            when {
                state.loading -> Text(stringResource(R.string.leaderboard_loading), color = TextSecondary)
                state.error != null -> {
                    Text(translatedErrorMessage(state.error) ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text(stringResource(R.string.leaderboard_retry)) }
                }
                state.entries.isEmpty() -> Text(stringResource(R.string.leaderboard_empty), color = TextSecondary)
                else -> {
                    state.entries.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${index + 1}. ${entry.name}")
                            Text("%,d".format(entry.score), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
