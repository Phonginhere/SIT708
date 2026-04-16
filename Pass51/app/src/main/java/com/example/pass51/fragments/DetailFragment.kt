package com.example.pass51.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R
import com.example.pass51.adapters.RelatedStoriesAdapter
import com.example.pass51.data.BookmarkManager
import com.example.pass51.data.DummyData
import com.example.pass51.data.NewsItem
import com.google.android.material.bottomnavigation.BottomNavigationView


class DetailFragment : Fragment() {

    private var newsItem: NewsItem? = null
    var onRelatedItemClick: ((NewsItem) -> Unit)? = null

    companion object {
        private const val ARG_NEWS_ID = "news_id"

        fun newInstance(newsId: Int): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putInt(ARG_NEWS_ID, newsId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val newsId = arguments?.getInt(ARG_NEWS_ID) ?: return
        newsItem = DummyData.getById(newsId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = newsItem ?: return
        val backButton = view.findViewById<TextView>(R.id.backButton)

        backButton.setOnClickListener {
            // Reach Activity view hierarchy from Fragment, then show bottom nav again
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
                ?.visibility = View.VISIBLE

            // Return to previous fragment
            parentFragmentManager.popBackStack()
        }


        // TODO: Find all views and bind data
        // Find the views using their ids from fragment_detail.xml (look back at that layout to remember the ids)
            val imageView = view.findViewById<ImageView>(R.id.largeImage)
            val titleView = view.findViewById<TextView>(R.id.newsTitle)
            val categoryView = view.findViewById<TextView>(R.id.categoryBadge)
            val descriptionView = view.findViewById<TextView>(R.id.newsDescription)
            val bookmarkButton = view.findViewById<ImageButton>(R.id.bookmarkButton)
            val relatedRecycler = view.findViewById<RecyclerView>(R.id.newsRecycler)

            // Set the image using getIdentifier like you did in the Adapters
            val resId = requireContext().resources.getIdentifier(
                item.imageUrl, "drawable", requireContext().packageName
            )
            imageView.setImageResource(resId)

            // Set the title, category, and description text
            titleView.text = item.title
            categoryView.text = item.category
            descriptionView.text = item.description

        if (BookmarkManager.isBookmarked(item.id)) {
            bookmarkButton.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            bookmarkButton.setImageResource(android.R.drawable.btn_star_big_off)
        }

            // Set up the bookmark button
            bookmarkButton.setOnClickListener {
                BookmarkManager.toggleBookmark(item.id)

                if (BookmarkManager.isBookmarked(item.id)) {
                    bookmarkButton.setImageResource(android.R.drawable.btn_star_big_on)
                    Toast.makeText(requireContext(), "Added to bookmarks", Toast.LENGTH_SHORT).show()
                } else {
                    bookmarkButton.setImageResource(android.R.drawable.btn_star_big_off)
                    Toast.makeText(requireContext(), "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                }
            }

            // Set up the related stories RecyclerView
            relatedRecycler.layoutManager =
                LinearLayoutManager(requireContext())
            val relatedItems = DummyData.getRelatedStories(item)
            val adapter = RelatedStoriesAdapter(relatedItems) { relatedItem ->
                onRelatedItemClick?.invoke(relatedItem)
            }
            relatedRecycler.adapter = adapter
    }
}