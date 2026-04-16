package com.example.pass51_istream.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pass51_istream.adapter.PlaylistAdapter
import com.example.pass51_istream.R
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.pass51_istream.database.AppDatabase
import kotlinx.coroutines.launch

class PlaylistFragment : Fragment() {
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: TextView
    private var userId: Int = -1

    var onPlaylistItemClick: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt("userId", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        recyclerView = view.findViewById(R.id.playlistRecyclerView)
        backButton = view.findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistAdapter = PlaylistAdapter(emptyList()) { item ->
            onPlaylistItemClick?.invoke(item.url)
        }
        recyclerView.adapter = playlistAdapter

        lifecycleScope.launch {
            val items = db.playlistDao().getPlaylistItemsForUser(userId)
            playlistAdapter.updateData(items)
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
