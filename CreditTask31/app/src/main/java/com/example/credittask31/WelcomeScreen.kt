package com.example.credittask31

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    returnedName: String = "",
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onStart: (String) -> Unit
) {
    var name by remember(returnedName) { mutableStateOf(returnedName) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top-right theme toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDarkMode) "☀️" else "🌙",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = isDarkMode,
                onCheckedChange = onThemeToggle
            )
        }

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SIT305 Quiz App",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter your name:",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    placeholder = { Text("Name") },
                    singleLine = true,
                    isError = showError,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showError) {
                Text(
                    text = "Please enter your name",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        showError = true
                    } else {
                        onStart(name.trim())
                    }
                },
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("START", fontSize = 18.sp)
            }
        }
    }
}