package com.a1exymoroz.mergefruit.ui.auth

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.common.LanguageSwitcher
import com.a1exymoroz.mergefruit.ui.theme.TextSecondary
import com.a1exymoroz.mergefruit.ui.theme.appBackground

/** Mirrors src/components/auth/GuestGateModal.tsx — shown at "/" when not authenticated and not a guest. */
@Composable
fun GuestGateScreen(
    authViewModel: AuthViewModel,
    onContinueAsGuest: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    Box(Modifier.fillMaxSize().appBackground(), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.width(340.dp).padding(16.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                LanguageSwitcher()
                Text(stringResource(R.string.common_app_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.auth_welcome_subtitle),
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        authViewModel.continueAsGuest()
                        onContinueAsGuest()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                ) {
                    Text(stringResource(R.string.auth_continue_as_guest))
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.auth_or), color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.auth_sign_in))
                }

                Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.auth_no_account_prefix))
                    TextButton(onClick = onNavigateToSignUp) { Text(stringResource(R.string.auth_sign_up)) }
                }

                Text(
                    stringResource(R.string.auth_guest_note),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
