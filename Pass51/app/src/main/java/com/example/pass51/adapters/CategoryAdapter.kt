package com.example.pass51.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0   // "All" selected by default

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: TextView = itemView.findViewById(R.id.categoryChip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.chip.text = category

        // TODO: set the background and text colour based on whether
        // this position equals selectedPosition
        // Selected: chip_selected background, white text
        // Unselected: chip_background background, dark text
        if (position == selectedPosition) {
            holder.chip.setBackgroundResource(R.drawable.chip_selected)
            holder.chip.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.chip.setBackgroundResource(R.drawable.chip_background)
            holder.chip.setTextColor(holder.itemView.context.getColor(R.color.black))
        }


        // TODO: set a click listener that:
        // 1. Updates selectedPosition to the tapped position
        // 2. Notifies the adapter to refresh the old and new positions
        // 3. Calls onCategorySelected with the category name
        holder.chip.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onCategorySelected(category)
        }
    }

    override fun getItemCount() = categories.size
}