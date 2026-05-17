package com.example.llm61.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["auth0Sub"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auth0Sub: String,
    val username: String,
    val email: String = "",
    val tier: String = "free",
    val tierPurchasedAt: Long = 0,
    val cancelledAt: Long = 0,      // >0 = scheduled change pending; access until tierPurchasedAt + 30 days
    val scheduledTier: String = "free" // tier to switch to when cancelledAt period expires
)