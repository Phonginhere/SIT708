package com.example.pass51.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object BookmarkManager {
    private const val PREFS_NAME = "bookmarks_prefs"
    private const val KEY_BOOKMARKS = "bookmarked_ids"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun toggleBookmark(newsId: Int) {
        val ids = getBookmarkedIds().toMutableSet()
        if (ids.contains(newsId.toString())) {
            ids.remove(newsId.toString())
        } else {
            ids.add(newsId.toString())
        }
        prefs.edit { putStringSet(KEY_BOOKMARKS, ids) }
    }

    fun isBookmarked(newsId: Int): Boolean {
        return getBookmarkedIds().contains(newsId.toString())
    }

    fun getBookmarkedStories(): List<NewsItem> {
        val ids = getBookmarkedIds().mapNotNull { it.toIntOrNull() }
        return DummyData.allNews.filter { it.id in ids }
    }

    private fun getBookmarkedIds(): Set<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
    }
}