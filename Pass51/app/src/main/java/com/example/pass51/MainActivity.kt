package com.example.pass51

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pass51.data.BookmarkManager
import com.example.pass51.data.NewsItem
import com.example.pass51.fragments.BookmarksFragment
import com.example.pass51.fragments.DetailFragment
import com.example.pass51.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.addCallback

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO 1: Initialise BookmarkManager
        BookmarkManager.init(this)

        // TODO 2: Find bottomNav by id
        bottomNav = findViewById(R.id.bottomNav)

        // TODO 3: Load HomeFragment (only on first launch)
        if (savedInstanceState == null) {
            showHomeFragment()
        }

        // TODO 4: Handle bottom nav item selection
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_id -> {
                    showHomeFragment()
                    true
                }

                R.id.bookmarks_id -> {
                    showBookmarksFragment()
                    true
                }

                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                bottomNav.visibility = android.view.View.VISIBLE
            } else {
                finish()
            }
        }
    }

    private fun showHomeFragment() {
        // TODO 5: Create HomeFragment, set its click callback,
        // and replace the fragmentContainer
        val homeFragment = HomeFragment()
        homeFragment.onNewsItemClick = { item ->
            navigateToDetail(item)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, homeFragment)
            .commit()
    }

    private fun showBookmarksFragment() {
        // TODO 6: Create BookmarksFragment, set its click callback,
        // and replace the fragmentContainer
        val bookmarksFragment = BookmarksFragment()
        bookmarksFragment.onNewsItemClick = { item ->
            navigateToDetail(item)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, bookmarksFragment)
            .commit()
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        // TODO 7: Create DetailFragment with the news id,
        // set its related item callback, hide bottomNav,
        // and replace fragmentContainer (add to back stack)
        val detailFragment = DetailFragment.newInstance(newsItem.id)
        detailFragment.onRelatedItemClick = { relatedItem ->
            navigateToDetail(relatedItem)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null)
            .commit()
        bottomNav.visibility = android.view.View.GONE
    }

}