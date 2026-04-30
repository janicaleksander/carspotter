package com.example.carspotter.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.carspotter.ui.components.AppBranding
import com.example.carspotter.ui.components.AppTextField
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.AuthUiState

@Composable
fun AuthContent(
    innerPadding: PaddingValues,
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onModeToggle: () -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        AppBranding()

        Spacer(modifier = Modifier.height(32.dp))

        AuthHeaderTexts(uiState = uiState)

        Spacer(modifier = Modifier.height(40.dp))

        AuthFormFields(
            uiState = uiState,
            focusManager = focusManager,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onNicknameChange = onNicknameChange,
            onPasswordVisibilityToggle = onPasswordVisibilityToggle,
            onSubmit = onSubmit,
        )

        Spacer(modifier = Modifier.height(36.dp))

        AuthSubmitButton(uiState = uiState, onSubmit = onSubmit)

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        AuthModeFooter(
            uiState = uiState,
            onModeToggle = onModeToggle,
        )
    }
}

@Composable
private fun AuthHeaderTexts(uiState: AuthUiState) {
    Text(
        text = if (uiState.isLoginMode) "Welcome Back" else "Create Account",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = if (uiState.isLoginMode) "Log in to track your garage"
        else "Sign up to start spotting",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AuthFormFields(
    uiState: AuthUiState,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
) {
    if (!uiState.isLoginMode) {
        AppTextField(
            label = "Nickname",
            value = uiState.nickname,
            onValueChange = onNicknameChange,
            placeholder = "john",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            enabled = !uiState.isLoading,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    AppTextField(
        label = "Email Address",
        value = uiState.email,
        onValueChange = onEmailChange,
        placeholder = "name@example.com",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
        ),
        enabled = !uiState.isLoading,
    )

    Spacer(modifier = Modifier.height(20.dp))

    AppTextField(
        label = "Password",
        value = uiState.password,
        onValueChange = onPasswordChange,
        placeholder = "Enter password",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
            )
        },
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityToggle) {
                Icon(
                    imageVector = if (uiState.passwordVisible) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    contentDescription = if (uiState.passwordVisible) "Hide password"
                    else "Show password",
                )
            }
        },
        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSubmit()
            },
        ),
        enabled = !uiState.isLoading,
    )
}

@Composable
private fun AuthSubmitButton(
    uiState: AuthUiState,
    onSubmit: () -> Unit,
) {
    Button(
        onClick = onSubmit,
        enabled = uiState.email.isNotBlank() &&
                uiState.password.isNotBlank() &&
                !uiState.isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CarRed,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = if (uiState.isLoginMode) "LOGIN" else "SIGN UP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = MaterialTheme.typography.titleMedium.letterSpacing * 1.4,
            )
        }
    }
}

@Composable
private fun AuthModeFooter(
    uiState: AuthUiState,
    onModeToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = if (uiState.isLoginMode) "New to the garage?" else "Already have an account?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TextButton(onClick = onModeToggle) {
            Text(
                text = if (uiState.isLoginMode) "Sign Up" else "Log In",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CarRed,
            )
        }
    }
}