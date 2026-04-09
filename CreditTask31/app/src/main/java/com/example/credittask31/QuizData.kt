package com.example.credittask31


object QuizData {
    fun getQuestions(): List<Question> = listOf(
        Question(
            title = "Android Basics",
            detail = "Which language is officially recommended for Android development?",
            options = listOf("Java", "Kotlin", "Swift"),
            correctIndex = 1
        ),
        Question(
            title = "Activity Lifecycle",
            detail = "Which callback is called when an Activity first becomes visible to the user?",
            options = listOf("onCreate()", "onResume()", "onStart()"),
            correctIndex = 2
        ),
        Question(
            title = "Layouts",
            detail = "Which layout arranges children in a single row or column?",
            options = listOf("LinearLayout", "ConstraintLayout", "FrameLayout"),
            correctIndex = 0
        ),
        Question(
            title = "Intents",
            detail = "Which type of Intent explicitly names the target component?",
            options = listOf("Implicit Intent", "Explicit Intent", "Broadcast Intent"),
            correctIndex = 1
        ),
        Question(
            title = "Storage",
            detail = "Which Android component is best for storing simple key-value pairs?",
            options = listOf("SQLite", "SharedPreferences", "ContentProvider"),
            correctIndex = 1
        )
    )
}