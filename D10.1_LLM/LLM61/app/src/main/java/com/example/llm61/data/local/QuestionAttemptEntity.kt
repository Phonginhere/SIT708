package com.example.llm61.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_attempts",
    foreignKeys = [ForeignKey(
        entity = QuizAttemptEntity::class,
        parentColumns = ["id"],
        childColumns = ["attemptId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("attemptId")]
)
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,                // FK to QuizAttemptEntity.id
    val questionText: String,
    val options: List<String>,          // converted to/from JSON via Converters
    val userSelectedIndex: Int?,        // null if unanswered
    val correctIndex: Int,
    val isCorrect: Boolean
)