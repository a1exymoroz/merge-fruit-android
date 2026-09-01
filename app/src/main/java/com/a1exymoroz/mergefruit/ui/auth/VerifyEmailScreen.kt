package com.a1exymoroz.mergefruit.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.common.LanguageSwitcher
import com.a1exymoroz.mergefruit.ui.common.translatedErrorMessage
import com.a1exymoroz.mergefruit.ui.theme.ErrorRed
import com.a1exymoroz.mergefruit.ui.theme.SuccessGreen
import com.a1exymoroz.mergefruit.ui.theme.appBackground
import kotlinx.coroutines.launch

private const val CODE_LENGTH = 4

/** Mirrors src/components/containers/VerifyEmailPage.tsx. */
@Composable
fun VerifyEmailScreen(
    token: String?,
    showCheckEmailMessage: Boolean,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onVerified: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val digits = remember { mutableStateListOf("", "", "", "") }
    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    val code = digits.joinToString("")
    val canSubmit = !token.isNullOrEmpty() && code.length == CODE_LENGTH && !submitting

    fun submit() {
        if (!canSubmit || token == null) return
        scope.launch {
            submitting = true
            error = null
            try {
                authViewModel.verifyEmail(token, code)
                authViewModel.markEmailVerified()
                verified = true
            } catch (e: Exception) {
                error = e.message
            } finally {
                submitting = false
            }
        }
    }

    Box(Modifier.fillMaxSize().appBackground(), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.width(340.dp).padding(16.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                LanguageSwitcher()
                Text(stringResource(R.string.common_app_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                when {
                    token.isNullOrEmpty() -> {
                        Text(stringResource(R.string.auth_verify_email), modifier = Modifier.padding(top = 4.dp))
                        Text(
                            stringResource(
                                if (showCheckEmailMessage) R.string.auth_check_email_message else R.string.auth_open_verification_link,
                            ),
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = onNavigateToLogin, modifier = Modifier.padding(top = 12.dp)) {
                            Text(stringResource(R.string.auth_back_to_sign_in))
                        }
                    }

                    verified -> {
                        Text(stringResource(R.string.auth_email_verified), modifier = Modifier.padding(top = 4.dp))
                        Text(stringResource(R.string.auth_email_verified_message), color = SuccessGreen, modifier = Modifier.padding(top = 12.dp))
                        Button(onClick = onVerified, modifier = Modifier.padding(top = 16.dp)) {
                            Text(stringResource(R.string.auth_continue_to_game))
                        }
                    }

                    else -> {
                        Text(stringResource(R.string.auth_verify_email), modifier = Modifier.padding(top = 4.dp))
                        Text(
                            stringResource(if (showCheckEmailMessage) R.string.auth_check_email_message else R.string.auth_enter_code),
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Center,
                        )

                        Row(modifier = Modifier.padding(top = 16.dp).wrapContentWidth()) {
                            digits.forEachIndexed { index, digit ->
                                OutlinedTextField(
                                    value = digit,
                                    onValueChange = { newValue ->
                                        val cleaned = newValue.filter { it.isDigit() }.takeLast(1)
                                        digits[index] = cleaned
                                        if (cleaned.isNotEmpty() && index < CODE_LENGTH - 1) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    enabled = !submitting,
                                    modifier = Modifier
                                        .width(56.dp)
                                        .padding(horizontal = 4.dp)
                                        .focusRequester(focusRequesters[index]),
                                )
                            }
                        }

                        error?.let {
                            Text(translatedErrorMessage(it) ?: it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp))
                        }

                        Button(onClick = { submit() }, enabled = canSubmit, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            Text(stringResource(if (submitting) R.string.auth_verifying else R.string.auth_verify_email_button))
                        }
                    }
                }
            }
        }
    }
}
