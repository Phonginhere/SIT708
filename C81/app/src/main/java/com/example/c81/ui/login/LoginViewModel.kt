// app/src/main/java/com/example/c81/ui/login/LoginViewModel.kt
package com.example.c81.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    var username by mutableStateOf("")
        private set

    fun onUsernameChange(value: String) {
        username = value
    }

    val canProceed: Boolean
        get() = username.isNotBlank()
}