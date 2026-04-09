package com.example.pass_task41.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface EventDao {

    //show in order by datetime
    @Query("SELECT * FROM events ORDER BY dateTime ASC")
    fun getUpcomingEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Int): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)
}