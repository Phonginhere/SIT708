package com.example.llm61.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.data.UserAnswer
import com.example.llm61.network.GeminiRepository
import com.example.llm61.network.LlmUiState
import kotlinx.coroutines.launch

class ResultsViewModel : ViewModel() {

    private val repo = GeminiRepository()

    // Map: questionId -> LLM state for that explanation
    var explanationStates by mutableStateOf<Map<Int, LlmUiState>>(emptyMap())
        private set

    fun requestExplanation(answer: UserAnswer) {
        val question = answer.question
        val userChoiceText = answer.selectedIndex
            ?.let { question.options[it] }
            ?: "(no answer)"
        val correctChoiceText = question.options[question.correctAnswerIndex]

        // Two different prompts — one for correct answers (reinforcement),
        // one for incorrect (explanation + correction)
        val prompt = if (answer.isCorrect) {
            buildString {
                append("You are a helpful tutor. The student answered this question correctly. ")
                append("Provide a short (2-3 sentences) explanation of WHY the answer is correct ")
                append("to reinforce their understanding.\n\n")
                append("Question: ${question.text}\n")
                append("Correct answer: $correctChoiceText\n")
            }
        } else {
            buildString {
                append("You are a helpful tutor. The student answered this question incorrectly. ")
                append("Explain in 2-3 sentences why their answer is wrong, then briefly ")
                append("explain why the correct answer is right. Be encouraging.\n\n")
                append("Question: ${question.text}\n")
                append("Student's answer: $userChoiceText\n")
                append("Correct answer: $correctChoiceText\n")
            }
        }

        // Show loading immediately
        explanationStates = explanationStates + (question.id to LlmUiState.Loading)

        viewModelScope.launch {
            repo.generate(prompt)
                .onSuccess { response ->
                    explanationStates = explanationStates + (question.id to LlmUiState.Success(prompt, response))
                }
                .onFailure { error ->
                    val friendlyMessage = when {
                        error.message?.contains("timeout", ignoreCase = true) == true ->
                            "Request took too long. Please try again."
                        error.message?.contains("429") == true ->
                            "Too many requests. Please wait a moment."
                        error.message?.contains("Unable to resolve host") == true ->
                            "No internet connection."
                        else ->
                            error.message ?: "Something went wrong."
                    }
                    explanationStates = explanationStates + (question.id to LlmUiState.Error(prompt, friendlyMessage))
                }
        }
    }

    fun clearExplanation(questionId: Int) {
        explanationStates = explanationStates - questionId
    }
}