package com.example.pass51_istream.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlaylistDao {
    // Insert a new playlist item
    @Insert
    suspend fun insertPlaylistItem(item: PlaylistItem)

    // Get all playlist items for a specific user
    @Query("SELECT * FROM playlist_items WHERE userId = :userId")
    suspend fun getPlaylistItemsForUser(userId: Int): List<PlaylistItem>
}