package com.example.llm61.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.network.GeminiRepository
import com.example.llm61.network.LlmUiState
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {

    // Single shared repository instance
    private val repo = GeminiRepository()

    // Map: questionId -> LLM state for that question's hint
    // Using a mutableStateOf<Map> so the UI re-renders when a single entry changes
    var hintStates by mutableStateOf<Map<Int, LlmUiState>>(emptyMap())
        private set

    fun requestHint(questionId: Int, questionText: String, options: List<String>) {
        // Build a focused prompt — the model gets the question and options,
        // but is told NOT to reveal the answer
        val prompt = buildString {
            append("You are a helpful tutor. Give a short, encouraging hint ")
            append("(1-2 sentences) for the following multiple-choice question. ")
            append("Do NOT reveal the correct answer directly. ")
            append("Guide the student's thinking instead.\n\n")
            append("Question: $questionText\n")
            append("Options:\n")
            options.forEachIndexed { idx, opt ->
                append("${('A' + idx)}. $opt\n")
            }
        }

        // Show loading immediately
        hintStates = hintStates + (questionId to LlmUiState.Loading)

        // Launch the API call on the IO dispatcher (handled inside Retrofit's suspend fun)
        viewModelScope.launch {
            repo.generate(prompt)
                .onSuccess { response ->
                    hintStates = hintStates + (questionId to LlmUiState.Success(prompt, response))
                }
                .onFailure { error ->
                    hintStates = hintStates + (questionId to LlmUiState.Error(prompt, error.message ?: "Unknown error"))
                }
        }
    }

    // Allow user to clear/hide a hint
    fun clearHint(questionId: Int) {
        hintStates = hintStates - questionId
    }
}