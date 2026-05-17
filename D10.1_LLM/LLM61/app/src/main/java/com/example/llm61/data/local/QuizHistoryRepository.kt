package com.example.llm61.data.local

import com.example.llm61.data.UserAnswer

class QuizHistoryRepository(private val dao: QuizAttemptDao) {

    fun allAttempts(userId: Long) = dao.observeAttemptsForUser(userId)
    fun totalQuestions(userId: Long) = dao.observeTotalQuestionsForUser(userId)
    fun totalCorrect(userId: Long) = dao.observeTotalCorrectForUser(userId)
    fun totalIncorrect(userId: Long) = dao.observeTotalIncorrectForUser(userId)
    fun observeQuestionsForAttempt(attemptId: Long) = dao.observeQuestionsForAttempt(attemptId)

    suspend fun getRecentIncorrectQuestions(userId: Long) =
        dao.getRecentIncorrectQuestionsForUser(userId)

    suspend fun saveAttempt(userId: Long, topic: String, answers: List<UserAnswer>): Long {
        val correctCount = answers.count { it.isCorrect }
        val incorrectCount = answers.size - correctCount

        val attempt = QuizAttemptEntity(
            userId = userId,
            topic = topic,
            timestamp = System.currentTimeMillis(),
            totalQuestions = answers.size,
            correctCount = correctCount,
            incorrectCount = incorrectCount
        )
        val questions = answers.map { ua ->
            QuestionAttemptEntity(
                attemptId = 0,
                questionText = ua.question.text,
                options = ua.question.options,
                userSelectedIndex = ua.selectedIndex,
                correctIndex = ua.question.correctAnswerIndex,
                isCorrect = ua.isCorrect
            )
        }
        return dao.saveAttemptWithQuestions(attempt, questions)
    }
}