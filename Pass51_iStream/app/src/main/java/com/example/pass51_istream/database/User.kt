package com.example.pass51_istream.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val fullName: String,
        val username: String,
        val password: String
)
