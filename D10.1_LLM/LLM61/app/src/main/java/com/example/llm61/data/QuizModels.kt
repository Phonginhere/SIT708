package com.example.llm61.data

// One multiple-choice question
data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int   // index into `options`
)

// A "task" is a topic + a set of questions for that topic
data class Task(
    val topic: String,
    val description: String,
    val questions: List<Question>
)

// A user's answer to a single question
data class UserAnswer(
    val question: Question,
    val selectedIndex: Int?,      // null if user didn't pick anything
    val isCorrect: Boolean
)