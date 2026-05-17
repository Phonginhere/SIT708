package com.example.llm61.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizAttemptDao {

    @Insert
    abstract suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Insert
    abstract suspend fun insertQuestionAttempts(questions: List<QuestionAttemptEntity>)

    @Transaction
    open suspend fun saveAttemptWithQuestions(
        attempt: QuizAttemptEntity,
        questions: List<QuestionAttemptEntity>
    ): Long {
        val attemptId = insertAttempt(attempt)
        val withId = questions.map { it.copy(attemptId = attemptId) }
        insertQuestionAttempts(withId)
        return attemptId
    }

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    abstract fun observeAttemptsForUser(userId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM question_attempts WHERE attemptId = :attemptId ORDER BY id ASC")
    abstract fun observeQuestionsForAttempt(attemptId: Long): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT IFNULL(SUM(totalQuestions), 0) FROM quiz_attempts WHERE userId = :userId")
    abstract fun observeTotalQuestionsForUser(userId: Long): Flow<Int>

    @Query("SELECT IFNULL(SUM(correctCount), 0) FROM quiz_attempts WHERE userId = :userId")
    abstract fun observeTotalCorrectForUser(userId: Long): Flow<Int>

    @Query("SELECT IFNULL(SUM(incorrectCount), 0) FROM quiz_attempts WHERE userId = :userId")
    abstract fun observeTotalIncorrectForUser(userId: Long): Flow<Int>

    @Query("""
        SELECT qa.* FROM question_attempts qa
        INNER JOIN quiz_attempts a ON a.id = qa.attemptId
        WHERE qa.isCorrect = 0 AND a.userId = :userId
        ORDER BY qa.id DESC
        LIMIT 20
    """)
    abstract suspend fun getRecentIncorrectQuestionsForUser(userId: Long): List<QuestionAttemptEntity>
}