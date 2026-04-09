package com.example.credittask31

data class Question(
    val title: String,
    val detail: String,
    val options: List<String>,
    val correctIndex: Int
)
