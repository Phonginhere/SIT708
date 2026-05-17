package com.example.llm61.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE auth0Sub = :sub LIMIT 1")
    suspend fun findByAuth0Sub(sub: String): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET tier = :tier, tierPurchasedAt = :purchasedAt WHERE id = :userId")
    suspend fun updateTierAndPurchaseDate(userId: Long, tier: String, purchasedAt: Long)

    @Query("SELECT tier FROM users WHERE id = :userId LIMIT 1")
    fun observeTier(userId: Long): kotlinx.coroutines.flow.Flow<String?>

    @Query("SELECT tierPurchasedAt FROM users WHERE id = :userId LIMIT 1")
    fun observeTierPurchasedAt(userId: Long): kotlinx.coroutines.flow.Flow<Long?>

    @Query("UPDATE users SET cancelledAt = :ts WHERE id = :userId")
    suspend fun updateCancelledAt(userId: Long, ts: Long)

    @Query("SELECT cancelledAt FROM users WHERE id = :userId LIMIT 1")
    fun observeCancelledAt(userId: Long): kotlinx.coroutines.flow.Flow<Long?>

    @Query("UPDATE users SET scheduledTier = :tier WHERE id = :userId")
    suspend fun updateScheduledTier(userId: Long, tier: String)

    @Query("SELECT scheduledTier FROM users WHERE id = :userId LIMIT 1")
    fun observeScheduledTier(userId: Long): kotlinx.coroutines.flow.Flow<String?>
}