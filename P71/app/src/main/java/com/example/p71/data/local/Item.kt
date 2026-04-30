package com.example.p71.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postType: String,        // "Lost" or "Found"
    val name: String,
    val phone: String,
    val description: String,
    val category: String,        // "Electronics", "Pets", "Wallets", etc.
    val date: String,            // User-selected date
    val location: String,
    val imageData: ByteArray?,   // Image stored as BLOB
    val createdAt: Long = System.currentTimeMillis()  // Auto timestamp
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Item) return false
        return id == other.id
    }

    override fun hashCode(): Int = id
}