package com.example.pass51_istream.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)


data class PlaylistItem (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var url : String = "",
    var userId : Int = 0
)

