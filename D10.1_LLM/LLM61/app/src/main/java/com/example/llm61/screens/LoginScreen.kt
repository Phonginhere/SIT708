package com.example.llm61.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.llm61.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome,", style = MaterialTheme.typography.titleLarge)
        Text("Student!", style = MaterialTheme.typography.displaySmall)
        Text("Let's Start Learning!", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(48.dp))

        Text(
            "Secure login powered by Auth0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val activity = context as? Activity ?: return@Button
                userViewModel.login(activity, onLoggedIn = onLoginSuccess)
            },
            enabled = !userViewModel.isAuthenticating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (userViewModel.isAuthenticating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(Modifier.width(12.dp))
                Text("Opening Auth0…")
            } else {
                Text("Login / Sign Up")
            }
        }

        if (userViewModel.authError != null) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Sign-in error", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        userViewModel.authError ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { userViewModel.clearAuthError() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}