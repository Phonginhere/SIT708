package com.example.pass_task41.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val title: String,
    val category: String, // "Work", "Personal", "Health", "Other"
    val location: String,
    val datetime: Long, // stored as epoch time in milliseconds # https://stackoverflow.com/questions/64333467/best-way-of-storing-date-time-in-kotlin

)
