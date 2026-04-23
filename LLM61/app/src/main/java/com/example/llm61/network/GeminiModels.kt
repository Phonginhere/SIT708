package com.example.llm61.network

// ============= Request shape =============
// Gemini expects a body like:
// { "contents": [ { "parts": [ { "text": "your prompt" } ] } ] }

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

// ============= Response shape =============
// Gemini returns:
// { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)