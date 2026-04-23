package com.example.llm61.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.llm61.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit
) {
    // Local state for input fields and error message
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome,",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Student!",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "Let's Start Learning!",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(48.dp))

        // Username input
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null   // clear error when user types
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Password input — hides characters
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Show error if validation fails
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        // Login button — basic empty-field validation only
        Button(
            onClick = {
                when {
                    username.isBlank() -> errorMessage = "Please enter a username"
                    password.isBlank() -> errorMessage = "Please enter a password"
                    else -> {
                        userViewModel.login(username)
                        onLoginSuccess()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onSignupClick) {
            Text("Need an Account?")
        }
    }
}