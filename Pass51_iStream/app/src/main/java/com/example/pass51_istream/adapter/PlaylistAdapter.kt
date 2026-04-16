package com.example.pass51_istream.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51_istream.database.PlaylistItem
import com.example.pass51_istream.R

class PlaylistAdapter (
    private var playlistItems : List<PlaylistItem>,
    private val onItemClick: (PlaylistItem) -> Unit
): RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val url : TextView = itemView.findViewById(R.id.ytbPlaylistUrl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val item = playlistItems[position]
        holder.url.text = item.url
        holder.itemView.setOnClickListener { onItemClick(item) }

    }

    override fun getItemCount(): Int = playlistItems.size

    fun updateData(newItems: List<PlaylistItem>) {
        playlistItems = newItems
        notifyDataSetChanged()
    }
}