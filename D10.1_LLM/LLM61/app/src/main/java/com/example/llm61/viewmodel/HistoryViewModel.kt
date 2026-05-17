package com.example.llm61.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.llm61.data.local.AppDatabase
import com.example.llm61.data.local.QuizHistoryRepository

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = QuizHistoryRepository(
        AppDatabase.getInstance(application).quizAttemptDao()
    )

    fun attemptsFor(userId: Long) = repo.allAttempts(userId)
    fun questionsFor(attemptId: Long) = repo.observeQuestionsForAttempt(attemptId)
}