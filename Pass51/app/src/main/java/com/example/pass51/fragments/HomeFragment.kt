package com.example.pass51.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass51.R
import com.example.pass51.adapters.CategoryAdapter
import com.example.pass51.adapters.FeaturedMatchAdapter
import com.example.pass51.adapters.NewsAdapter
import com.example.pass51.data.DummyData
import com.example.pass51.data.NewsItem

class HomeFragment : Fragment() {

    // --- Properties (at the top) ---
    private lateinit var featuredAdapter: FeaturedMatchAdapter
    private lateinit var newsAdapter: NewsAdapter
    private var selectedCategory = "All"
    private var searchQuery = ""
    var onNewsItemClick: ((NewsItem) -> Unit)? = null

    // --- Function 1: onCreateView ---
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // --- Function 2: onViewCreated (just calls other functions) ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryFilter(view)
        setupFeaturedRecycler(view)
        setupNewsRecycler(view)
        setupSearchBar(view)
    }

    // --- Function 3: setupCategoryFilter ---
    private fun setupCategoryFilter(view: View) {
        val categoryRecycler = view.findViewById<RecyclerView>(R.id.categoryRecycler)
        categoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = CategoryAdapter(DummyData.categories) { category ->
            selectedCategory = category
            applyFilters()
        }
        categoryRecycler.adapter = adapter
    }

    // --- TODO: Function 4: setupFeaturedRecycler ---
    private fun setupFeaturedRecycler(view: View) {
        val featuredRecycler = view.findViewById<RecyclerView>(R.id.featuredRecycler)
        featuredRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        featuredAdapter = FeaturedMatchAdapter(DummyData.featuredStories) { item ->
            onNewsItemClick?.invoke(item)
        }
        featuredRecycler.adapter = featuredAdapter
    }

    // --- TODO: Function 5: setupNewsRecycler ---
    private fun setupNewsRecycler(view: View) {
        val newsRecycler = view.findViewById<RecyclerView>(R.id.newsRecycler)
        newsRecycler.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(DummyData.latestNews) { item ->
            onNewsItemClick?.invoke(item)
        }
        newsRecycler.adapter = newsAdapter
    }

    // --- TODO: Function 6: setupSearchBar ---
    private fun setupSearchBar(view: View) {
        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                applyFilters()
            }
        })
    }

    // --- TODO: Function 7: applyFilters  ---
    private fun applyFilters() {
        // Step 1: filter by category (includes ALL items — featured and non-featured)
        val filtered = DummyData.filterByCategory(selectedCategory)

        // Step 2: filter by search query (if not empty)
        val searchFiltered = if (searchQuery.isEmpty()) {
            filtered
        } else {
            filtered.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

        // Step 3: split into featured and non-featured
        val featuredFiltered = searchFiltered.filter { it.isFeatured }
        val newsFiltered = searchFiltered.filter { !it.isFeatured }

        // Step 4: update both adapters
        featuredAdapter.updateData(featuredFiltered)
        newsAdapter.updateData(newsFiltered)

    }

}
