package ru.acousticlive.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.acousticlive.app.ui.theme.AcousticLiveTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults


private val LoginBackground = Color(0xFF0F0F12)
private val LoginText = Color(0xFFF5F5F5)
private val LoginAccent = Color(0xFFFFB347)
private val LoginSecondary = Color(0xFF9A9AA5)

@Composable
fun OrganizerLoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by rememberSaveable { mutableStateOf("") }

    // Пароль храним только в памяти этого экрана.
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val canSubmit =
        email.isNotBlank() && password.isNotEmpty() && !isLoading

    Surface(
        modifier = modifier.fillMaxSize(),
        color = LoginBackground,
        contentColor = LoginText
       // color = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LoginAccent
                ),
            ) {
                Text("← К экрану зрителя")
            }

            Text(
                text = "Вход организатора",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Войдите, чтобы управлять каталогом и переключать песни.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LoginText,
                    unfocusedTextColor = LoginText,
                    disabledTextColor = LoginSecondary,
                    cursorColor = LoginAccent,
                    focusedBorderColor = LoginAccent,
                    unfocusedBorderColor = LoginSecondary,
                    focusedLabelColor = LoginAccent,
                    unfocusedLabelColor = LoginSecondary,
                    focusedContainerColor = LoginBackground,
                    unfocusedContainerColor = LoginBackground,
                    disabledContainerColor = LoginBackground
                ),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Пароль") },
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LoginText,
                    unfocusedTextColor = LoginText,
                    disabledTextColor = LoginSecondary,
                    cursorColor = LoginAccent,
                    focusedBorderColor = LoginAccent,
                    unfocusedBorderColor = LoginSecondary,
                    focusedLabelColor = LoginAccent,
                    unfocusedLabelColor = LoginSecondary,
                    focusedContainerColor = LoginBackground,
                    unfocusedContainerColor = LoginBackground,
                    disabledContainerColor = LoginBackground
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LoginAccent
                        ),
                    ) {
                        Text(
                            if (passwordVisible) "Скрыть" else "Показать"
                        )
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    onLogin(email.trim(), password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginAccent,
                    contentColor = LoginBackground,
                    disabledContainerColor = LoginAccent.copy(alpha = 0.3f),
                    disabledContentColor = LoginText.copy(alpha = 0.5f)
                )
            ) {
                Text(if (isLoading) "Входим…" else "Войти")
            }

            if (isLoading) {
                CircularProgressIndicator(color = LoginAccent)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrganizerLoginScreenPreview() {
    AcousticLiveTheme {
        OrganizerLoginScreen(
            onLogin = { _, _ -> },
            onBack = {}
        )
    }
}