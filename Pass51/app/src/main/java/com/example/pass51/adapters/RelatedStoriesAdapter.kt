package com.example.pass51.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R
import com.example.pass51.data.NewsItem

class RelatedStoriesAdapter(
    private var items: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<RelatedStoriesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: find the three views from item_related_story.xml
        val title: TextView = itemView.findViewById(R.id.relatedTitle)
        val category: TextView = itemView.findViewById(R.id.relatedCategory)
        val image: ImageView = itemView.findViewById(R.id.relatedImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // TODO: inflate R.layout.item_related_story
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_related_story, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: get the item, set title, category, image, and click listener
        val item = items[position]
        holder.title.text = item.title
        holder.category.text = item.category
        // For simplicity, we will not load the image from URL in this example.
        // In a real app, you would use an image loading library like Glide or Picasso.
        val resId = holder.itemView.context.resources.getIdentifier(
            item.imageUrl, "drawable", holder.itemView.context.packageName
        )
        holder.image.setImageResource(resId)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

}