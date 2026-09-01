package com.a1exymoroz.mergefruit.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.theme.TextSecondary

/** Mirrors src/components/ui/ColdStartNotice.tsx. */
@Composable
fun ColdStartNotice(isColdStart: Boolean, waiting: Boolean = false, modifier: Modifier = Modifier) {
    if (!isColdStart) return

    Text(
        text = stringResource(if (waiting) R.string.common_cold_start_waiting else R.string.common_cold_start_notice),
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(vertical = 8.dp),
    )
}
