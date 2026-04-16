package com.example.pass51.data

import java.io.Serializable

data class NewsItem  (
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val isFeatured: Boolean = false
): Serializable