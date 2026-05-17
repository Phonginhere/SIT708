package com.example.llm61.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.data.local.AppDatabase
import com.example.llm61.data.local.QuizHistoryRepository
import com.example.llm61.network.GeminiRepository
import com.example.llm61.network.LlmUiState
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepo = QuizHistoryRepository(
        AppDatabase.getInstance(application).quizAttemptDao()
    )
    private val gemini = GeminiRepository()

    fun totalQuestions(userId: Long) = historyRepo.totalQuestions(userId)
    fun totalCorrect(userId: Long) = historyRepo.totalCorrect(userId)
    fun totalIncorrect(userId: Long) = historyRepo.totalIncorrect(userId)

    var aiSummaryState by mutableStateOf<LlmUiState>(LlmUiState.Idle)
        private set

    fun requestAiSummary(userId: Long) {
        aiSummaryState = LlmUiState.Loading
        viewModelScope.launch {
            val incorrectQs = historyRepo.getRecentIncorrectQuestions(userId)

            if (incorrectQs.isEmpty()) {
                aiSummaryState = LlmUiState.Success(
                    prompt = "(No incorrect answers yet)",
                    response = "Great work — you haven't gotten any questions wrong yet! Keep going."
                )
                return@launch
            }

            val prompt = buildString {
                append("You are a learning coach. Below are recent questions a student got wrong. ")
                append("Identify 2-3 specific knowledge areas they should focus on, with one ")
                append("concrete study recommendation each. Be encouraging. Keep it under 150 words.\n\n")
                append("Incorrect questions:\n")
                incorrectQs.take(15).forEachIndexed { i, q ->
                    val userChoice = q.userSelectedIndex?.let { q.options[it] } ?: "(no answer)"
                    val correct = q.options[q.correctIndex]
                    append("${i + 1}. ${q.questionText}\n")
                    append("   Their answer: $userChoice\n")
                    append("   Correct: $correct\n")
                }
            }

            gemini.generate(prompt)
                .onSuccess { response ->
                    aiSummaryState = LlmUiState.Success(prompt, response)
                }
                .onFailure { error ->
                    aiSummaryState = LlmUiState.Error(
                        prompt = prompt,
                        message = error.message ?: "Couldn't get summary"
                    )
                }
        }
    }

    fun clearAiSummary() {
        aiSummaryState = LlmUiState.Idle
    }
}