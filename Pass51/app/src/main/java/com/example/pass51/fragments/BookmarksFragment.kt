package com.example.pass51.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R
import com.example.pass51.adapters.NewsAdapter
import com.example.pass51.data.BookmarkManager
import com.example.pass51.data.NewsItem

class BookmarksFragment : Fragment() {

    private lateinit var bookmarksAdapter: NewsAdapter
    private lateinit var emptyMessage: TextView
    private lateinit var bookmarksRecycler: RecyclerView

    var onNewsItemClick: ((NewsItem) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // TODO: inflate fragment_bookmarks
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: find views, set up RecyclerView, and load bookmarks
        emptyMessage = view.findViewById(R.id.noBookmarksMessage)
        bookmarksRecycler = view.findViewById(R.id.bookmarksRecyclerView)
        bookmarksRecycler.layoutManager = LinearLayoutManager(requireContext())
        bookmarksAdapter = NewsAdapter(emptyList()) { item ->
            onNewsItemClick?.invoke(item)
        }
            bookmarksRecycler.adapter = bookmarksAdapter
            loadBookmarks()
    }

    override fun onResume() {
        super.onResume()
        // TODO: refresh bookmarks when fragment becomes visible
        loadBookmarks()
    }

    private fun loadBookmarks() {
        // TODO: get bookmarked stories, update adapter,
        // and toggle visibility of recycler vs empty message
        val bookmarks = BookmarkManager.getBookmarkedStories()
        if (bookmarks.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            bookmarksRecycler.visibility = View.GONE
        } else {
            emptyMessage.visibility = View.GONE
            bookmarksRecycler.visibility = View.VISIBLE
            bookmarksAdapter.updateData(bookmarks)
            bookmarksRecycler.adapter = bookmarksAdapter
        }
    }
}