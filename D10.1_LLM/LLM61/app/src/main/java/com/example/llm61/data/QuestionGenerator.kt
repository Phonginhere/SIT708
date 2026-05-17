package com.example.llm61.data

import com.example.llm61.network.GeminiRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Uses Gemini to generate additional quiz questions on demand.
 * Returns strict JSON via prompt engineering, then parses with Gson.
 */
class QuestionGenerator(private val gemini: GeminiRepository = GeminiRepository()) {

    private data class GeneratedQuestion(
        val text: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )

    suspend fun generateExtraQuestions(
        topic: String,
        count: Int,
        existingQuestions: List<Question>
    ): Result<List<Question>> {
        if (count <= 0) return Result.success(emptyList())

        val prompt = buildPrompt(topic, count, existingQuestions)
        return gemini.generate(prompt).mapCatching { response ->
            parseQuestions(response, idOffset = 1000)
        }
    }

    fun buildPromptPublic(
        topic: String,
        count: Int,
        existing: List<Question>
    ): String = buildPrompt(topic, count, existing)

    private fun buildPrompt(
        topic: String,
        count: Int,
        existing: List<Question>
    ): String = buildString {
        appendLine("Generate exactly $count additional multiple-choice questions about $topic.")
        appendLine("Each question must have exactly 4 plausible options and ONE correct answer.")
        appendLine("Output STRICT JSON only — no markdown fences, no commentary, no preamble.")
        appendLine()
        appendLine("Format (a JSON array):")
        appendLine("[")
        appendLine("  {")
        appendLine("    \"text\": \"question text here\",")
        appendLine("    \"options\": [\"option A\", \"option B\", \"option C\", \"option D\"],")
        appendLine("    \"correctAnswerIndex\": 0")
        appendLine("  }")
        appendLine("]")
        appendLine()
        appendLine("correctAnswerIndex is 0-indexed (0 = first option is correct).")
        if (existing.isNotEmpty()) {
            appendLine()
            appendLine("Avoid duplicating these existing questions:")
            existing.forEach { q -> appendLine("- ${q.text}") }
        }
        appendLine()
        append("Return ONLY the JSON array, nothing else.")
    }

    private fun parseQuestions(rawResponse: String, idOffset: Int): List<Question> {
        // Strip markdown fences if Gemini added them despite the instructions
        val cleaned = rawResponse
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // Find the JSON array boundaries (in case of stray text)
        val start = cleaned.indexOf('[')
        val end = cleaned.lastIndexOf(']')
        if (start == -1 || end == -1 || end <= start) {
            error("No JSON array found in Gemini response")
        }
        val json = cleaned.substring(start, end + 1)

        val type = object : TypeToken<List<GeneratedQuestion>>() {}.type
        val parsed: List<GeneratedQuestion> = Gson().fromJson(json, type)

        return parsed.mapIndexed { idx, q ->
            require(q.options.size == 4) { "Question must have exactly 4 options" }
            require(q.correctAnswerIndex in 0..3) { "correctAnswerIndex must be 0..3" }
            Question(
                id = idOffset + idx,
                text = q.text,
                options = q.options,
                correctAnswerIndex = q.correctAnswerIndex
            )
        }
    }
}