package com.example.llm61.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.llm61.data.Question
import com.example.llm61.data.QuestionBank
import com.example.llm61.data.QuestionGenerator
import com.example.llm61.network.GeminiRepository
import com.example.llm61.network.LlmUiState
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {

    private val repo = GeminiRepository()
    private val generator = QuestionGenerator(repo)

    // ============= Quiz state =============
    var topic by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var bankQuestions by mutableStateOf<List<Question>>(emptyList())
        private set
    var extraQuestions by mutableStateOf<List<Question>>(emptyList())
        private set
    var extraQuestionsState: ExtraQuestionsState by mutableStateOf(ExtraQuestionsState.Idle)
        private set

    val allQuestions: List<Question>
        get() = bankQuestions + extraQuestions

    private var hasLoaded = false
    private var currentTier: String = "free"

    fun loadQuiz(taskId: String, tier: String) {
        val task = QuestionBank.getTaskForTopic(taskId)

        // Skip if already loaded with the same topic AND tier
        if (topic == task.topic && currentTier == tier && bankQuestions.isNotEmpty()) {
            return
        }

        currentTier = tier
        topic = task.topic
        description = task.description
        bankQuestions = task.questions
        extraQuestions = emptyList()

        val extraCount = extraQuestionsForTier(tier)
        if (extraCount <= 0) {
            extraQuestionsState = ExtraQuestionsState.NotNeeded
            return
        }

        fetchExtras(extraCount)
    }

    fun retryExtraQuestions() {
        val count = extraQuestionsForTier(currentTier)
        if (count <= 0) return
        fetchExtras(count)
    }

    private fun fetchExtras(count: Int) {
        extraQuestionsState = ExtraQuestionsState.Loading
        viewModelScope.launch {
            generator.generateExtraQuestions(
                topic = topic,
                count = count,
                existingQuestions = bankQuestions
            ).onSuccess { questions ->
                extraQuestions = questions
                extraQuestionsState = ExtraQuestionsState.Loaded
            }.onFailure { error ->
                extraQuestionsState = ExtraQuestionsState.Error(
                    error.message ?: "Failed to generate extra questions"
                )
            }
        }
    }

    private fun extraQuestionsForTier(tier: String): Int = when (tier) {
        "intermediate" -> 2  // 3 from bank + 2 generated = 5
        "advanced" -> 7      // 3 from bank + 7 generated = 10
        else -> 0            // free, starter
    }

    // ============= Existing hint code =============
    var hintStates by mutableStateOf<Map<Int, LlmUiState>>(emptyMap())
        private set

    fun requestHint(questionId: Int, questionText: String, options: List<String>) {
        val prompt = buildString {
            append("You are a helpful tutor. Give a short, encouraging hint ")
            append("(1-2 sentences) for the following multiple-choice question. ")
            append("Do NOT reveal the correct answer directly. ")
            append("Guide the student's thinking instead.\n\n")
            append("Question: $questionText\n")
            append("Options:\n")
            options.forEachIndexed { idx, opt -> append("${('A' + idx)}. $opt\n") }
        }
        hintStates = hintStates + (questionId to LlmUiState.Loading)
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

    fun clearHint(questionId: Int) {
        hintStates = hintStates - questionId
    }
}

sealed interface ExtraQuestionsState {
    object Idle : ExtraQuestionsState
    object NotNeeded : ExtraQuestionsState
    object Loading : ExtraQuestionsState
    object Loaded : ExtraQuestionsState
    data class Error(val message: String) : ExtraQuestionsState
}