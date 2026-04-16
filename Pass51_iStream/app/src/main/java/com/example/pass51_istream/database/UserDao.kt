package com.example.pass51_istream.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    // Sign up: insert a new user
    @Insert
    suspend fun insertUser(user: User)

    // Login: find a user by username and password
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUser(username: String, password: String): User?

    // Optional: Get user by userName (for session management)
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}