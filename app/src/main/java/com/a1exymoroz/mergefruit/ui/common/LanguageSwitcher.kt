package com.a1exymoroz.mergefruit.ui.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.a1exymoroz.mergefruit.R

private val SUPPORTED_LANGUAGES = listOf("en", "pl", "ru")

/**
 * Native equivalent of src/components/ui/LanguageSwitcher.tsx (which drove
 * i18next). Android's per-app language API (AppCompatDelegate) recreates
 * activities on change and resolves values-XX resources automatically.
 */
@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val currentTag = AppCompatDelegate.getApplicationLocales().takeIf { !it.isEmpty }
        ?.get(0)?.language ?: "en"

    OutlinedButton(
        onClick = { expanded = true },
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text("🌐", fontSize = 14.sp)
        Text(
            languageCode(currentTag),
            modifier = Modifier.padding(start = 6.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        SUPPORTED_LANGUAGES.forEach { tag ->
            val selected = tag == currentTag
            DropdownMenuItem(
                text = {
                    Text(
                        languageLabel(tag),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    )
                },
                leadingIcon = if (selected) { { CheckMark() } } else null,
                onClick = {
                    expanded = false
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                },
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Composable
private fun CheckMark() {
    Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable
private fun languageLabel(tag: String): String = when (tag) {
    "pl" -> stringResource(R.string.language_pl)
    "ru" -> stringResource(R.string.language_ru)
    else -> stringResource(R.string.language_en)
}

private fun languageCode(tag: String): String = tag.uppercase()
