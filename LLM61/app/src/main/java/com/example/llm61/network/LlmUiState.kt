package com.example.llm61.network

// Represents the four states the AI helper UI can be in:
// - Idle: no request made yet
// - Loading: request in flight, show spinner
// - Success: got a response, show prompt + response
// - Error: request failed, show error message
sealed interface LlmUiState {
    data object Idle : LlmUiState
    data object Loading : LlmUiState
    data class Success(val prompt: String, val response: String) : LlmUiState
    data class Error(val prompt: String, val message: String) : LlmUiState
}