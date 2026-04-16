package com.example.pass51.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R
import com.example.pass51.data.NewsItem

class NewsAdapter(
    private var items: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: find the three views from item_news.xml
        val title: TextView = itemView.findViewById(R.id.newsTitle)
        val category: TextView = itemView.findViewById(R.id.newsCategory)
        val image: ImageView = itemView.findViewById(R.id.newsImage)
        val description: TextView = itemView.findViewById(R.id.newsDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // TODO: inflate R.layout.item_news
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: get the item, set title, category, image, and click listener
        val item = items[position]
        holder.title.text = item.title
        holder.category.text = item.category
        holder.description.text = item.description
        // For simplicity, we will not load the image from URL in this example.
        // In a real app, you would use an image loading library like Glide or Picasso.
        val resId = holder.itemView.context.resources.getIdentifier(
            item.imageUrl, "drawable", holder.itemView.context.packageName
        )
        holder.image.setImageResource(resId)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}