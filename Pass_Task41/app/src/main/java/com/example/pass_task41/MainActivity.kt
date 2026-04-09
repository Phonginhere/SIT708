package com.example.pass_task41

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.pass_task41.ui.AddEditEventFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : FragmentActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private fun updateNavLabels(selectedItemId: Int) {
        val labels = mapOf(
            R.id.eventListFragment to "Home",
            R.id.nav_add_event to "Event",
            R.id.nav_add_trip to "Trip",
            R.id.nav_add_appointment to "Appointment"
        )
        for (i in 0 until bottomNav.menu.size()) {
            val menuItem = bottomNav.menu.getItem(i)
            menuItem.title = if (menuItem.itemId == selectedItemId) "" else labels[menuItem.itemId] ?: ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav = findViewById(R.id.bottom_nav)

        // Track current selected item to prevent re-clicking
        var currentSelectedId = R.id.eventListFragment

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentSelectedId) {
                return@setOnItemSelectedListener false
            }

            val currentDest = navController.currentDestination?.id
            val isOnAddEdit = currentDest == R.id.addEditEventFragment || currentDest == R.id.editEventFragment

            if (isOnAddEdit) {
                val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
                val addEditFragment = currentFragment as? AddEditEventFragment

                if (addEditFragment?.hasUnsavedChanges == true) {
                    // Trigger the dialog in the composable
                    addEditFragment.showExitDialogTrigger.value = true
                    return@setOnItemSelectedListener false
                }

                // No changes — navigate freely
                if (item.itemId == R.id.eventListFragment) {
                    navController.popBackStack(R.id.eventListFragment, false)
                    currentSelectedId = item.itemId
                    updateNavLabels(item.itemId)
                    return@setOnItemSelectedListener true
                }

                // Switching between add screens
                navController.popBackStack()
                currentSelectedId = item.itemId
                updateNavLabels(item.itemId)

                when (item.itemId) {
                    R.id.nav_add_event -> {
                        navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                            putString("category", "Event")
                        })
                    }
                    R.id.nav_add_trip -> {
                        navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                            putString("category", "Trip")
                        })
                    }
                    R.id.nav_add_appointment -> {
                        navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                            putString("category", "Appointment")
                        })
                    }
                }
                return@setOnItemSelectedListener true
            }

            currentSelectedId = item.itemId
            updateNavLabels(item.itemId)

            when (item.itemId) {
                R.id.eventListFragment -> {
                    navController.popBackStack(R.id.eventListFragment, false)
                    true
                }
                R.id.nav_add_event -> {
                    navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                        putString("category", "Event")
                    })
                    true
                }
                R.id.nav_add_trip -> {
                    navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                        putString("category", "Trip")
                    })
                    true
                }
                R.id.nav_add_appointment -> {
                    navController.navigate(R.id.addEditEventFragment, Bundle().apply {
                        putString("category", "Appointment")
                    })
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val itemId = when (destination.id) {
                R.id.eventListFragment -> R.id.eventListFragment
                R.id.addEditEventFragment, R.id.editEventFragment -> {
                    when (arguments?.getString("category")) {
                        "Event" -> R.id.nav_add_event
                        "Trip" -> R.id.nav_add_trip
                        "Appointment" -> R.id.nav_add_appointment
                        else -> R.id.nav_add_event
                    }
                }
                else -> R.id.eventListFragment
            }
            currentSelectedId = itemId
            updateNavLabels(itemId)
            bottomNav.menu.findItem(itemId)?.isChecked = true
        }
    }
}