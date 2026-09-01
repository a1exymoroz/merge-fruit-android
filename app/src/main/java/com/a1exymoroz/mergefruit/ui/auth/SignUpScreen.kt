package com.a1exymoroz.mergefruit.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.ui.common.ColdStartNotice
import com.a1exymoroz.mergefruit.ui.common.LanguageSwitcher
import com.a1exymoroz.mergefruit.ui.common.translatedErrorMessage
import com.a1exymoroz.mergefruit.ui.theme.ErrorRed
import com.a1exymoroz.mergefruit.ui.theme.appBackground
import kotlinx.coroutines.launch

/** Mirrors src/components/containers/SignUpPage.tsx. */
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (token: String?, checkEmail: Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isColdStart by authViewModel.isColdStart.collectAsStateWithLifecycle()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { authViewModel.warmUpBackend() }

    fun submit() {
        if (submitting) return
        scope.launch {
            submitting = true
            error = null
            try {
                val user = authViewModel.signUp(email.trim(), password, displayName.trim())
                if (!user.emailVerified && user.verificationToken != null) {
                    onNavigateToVerify(user.verificationToken, false)
                } else {
                    onSignUpSuccess()
                }
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
                Text(stringResource(R.string.auth_create_account), modifier = Modifier.padding(top = 4.dp))

                if (!submitting) ColdStartNotice(isColdStart)

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.auth_display_name)) },
                    placeholder = { Text(stringResource(R.string.auth_display_name_placeholder)) },
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.common_email)) },
                    placeholder = { Text(stringResource(R.string.common_email_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.common_password)) },
                    placeholder = { Text(stringResource(R.string.common_password_placeholder)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                error?.let {
                    Text(translatedErrorMessage(it) ?: it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp))
                }
                if (submitting) ColdStartNotice(isColdStart, waiting = true)

                Button(onClick = { submit() }, enabled = !submitting, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(stringResource(if (submitting) R.string.auth_creating_account else R.string.auth_sign_up))
                }

                Row(modifier = Modifier.padding(top = 12.dp)) {
                    Text(stringResource(R.string.auth_has_account_prefix))
                    TextButton(onClick = onNavigateToLogin) { Text(stringResource(R.string.auth_sign_in)) }
                }
            }
        }
    }
}
