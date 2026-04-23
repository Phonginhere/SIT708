package com.example.llm61.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.llm61.data.UserAnswer

// Shared ViewModel that holds the logged-in user's data.
// Survives navigation between screens (as long as we use the same instance).
class UserViewModel : ViewModel() {

    // The user's chosen username — shown on the Home greeting
    var username by mutableStateOf("")
        private set

    // The user's email (collected at signup)
    var email by mutableStateOf("")
        private set

    // The set of interests they picked (e.g., "Algorithms", "Web Development")
    // Using a Set so duplicates aren't possible
    var selectedInterests by mutableStateOf(setOf<String>())
        private set

    // Whether the user is "logged in" (controls navigation flow)
    var isLoggedIn by mutableStateOf(false)
        private set

    fun login(name: String) {
        username = name
        isLoggedIn = true
    }

    fun signup(name: String, userEmail: String) {
        username = name
        email = userEmail
    }

    fun toggleInterest(topic: String) {
        // If already selected, remove it; otherwise add it (max 10)
        selectedInterests = if (topic in selectedInterests) {
            selectedInterests - topic
        } else if (selectedInterests.size < 10) {
            selectedInterests + topic
        } else {
            selectedInterests   // ignore — already at max
        }
    }

    fun completeOnboarding() {
        isLoggedIn = true
    }

    fun logout() {
        username = ""
        email = ""
        selectedInterests = emptySet()
        isLoggedIn = false
    }

    var lastQuizAnswers by mutableStateOf<List<UserAnswer>>(emptyList())
        private set

    var lastQuizTopic by mutableStateOf("")
        private set

    fun submitQuiz(topic: String, answers: List<UserAnswer>) {
        lastQuizTopic = topic
        lastQuizAnswers = answers
    }
}