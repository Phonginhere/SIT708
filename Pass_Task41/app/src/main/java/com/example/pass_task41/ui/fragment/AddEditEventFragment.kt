package com.example.pass_task41.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pass_task41.R
import com.example.pass_task41.data.local.EventViewModel
import com.example.pass_task41.ui.theme.Pass_Task41Theme
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.compose.runtime.mutableStateOf

class AddEditEventFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels()
    var hasUnsavedChanges = false
    var showExitDialogTrigger = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val category = arguments?.getString("category")
        val eventId = arguments?.getInt("eventId", -1) ?: -1
        val isEditing = eventId != -1

        return ComposeView(requireContext()).apply {
            val fragment = this@AddEditEventFragment
            setContent {
                Pass_Task41Theme {
                    if (isEditing) {
                        var existingEvent by remember { mutableStateOf<com.example.pass_task41.data.local.Event?>(null) }

                        LaunchedEffect(eventId) {
                            existingEvent = viewModel.getById(eventId)
                        }

                        existingEvent?.let { event ->
                            AddEditEventScreen(
                                existingEvent = event,
                                fixedCategory = null,
                                onSave = { updated ->
                                    viewModel.update(updated)
                                    Toast.makeText(requireContext(), "Edit successful", Toast.LENGTH_SHORT).show()
                                    hasUnsavedChanges = false
                                    fragment.goHome()
                                },
                                onBack = {
                                    hasUnsavedChanges = false
                                    fragment.goHome()
                                },
                                onCategoryChanged = { cat ->
                                    val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
                                    bottomNav?.let { nav ->
                                        // Restore all labels
                                        for (i in 0 until nav.menu.size()) {
                                            nav.menu.getItem(i).title = when (nav.menu.getItem(i).itemId) {
                                                R.id.eventListFragment -> "Home"
                                                R.id.nav_add_event -> "Event"
                                                R.id.nav_add_trip -> "Trip"
                                                R.id.nav_add_appointment -> "Appointment"
                                                else -> ""
                                            }
                                        }

                                        val itemId = when (cat) {
                                            "Event" -> R.id.nav_add_event
                                            "Trip" -> R.id.nav_add_trip
                                            "Appointment" -> R.id.nav_add_appointment
                                            else -> null
                                        }
                                        itemId?.let {
                                            nav.menu.findItem(it)?.isChecked = true
                                            nav.menu.findItem(it)?.title = ""
                                        }
                                    }
                                },
                                onHasChanges = { hasUnsavedChanges = it },
                                externalShowExitDialog = fragment.showExitDialogTrigger
                            )
                        }
                    } else {
                        AddEditEventScreen(
                            existingEvent = null,
                            fixedCategory = category ?: "Event",
                            onSave = { event ->
                                viewModel.insert(event)
                                Toast.makeText(requireContext(), "Add successful", Toast.LENGTH_SHORT).show()
                                hasUnsavedChanges = false
                                fragment.goHome()
                            },
                            onBack = {
                                hasUnsavedChanges = false
                                fragment.goHome()
                            },
                            onHasChanges = { hasUnsavedChanges = it },
                            externalShowExitDialog = fragment.showExitDialogTrigger
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val category = arguments?.getString("category")
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav?.let { nav ->
            // Restore all labels
            for (i in 0 until nav.menu.size()) {
                nav.menu.getItem(i).title = when (nav.menu.getItem(i).itemId) {
                    R.id.eventListFragment -> "Home"
                    R.id.nav_add_event -> "Event"
                    R.id.nav_add_trip -> "Trip"
                    R.id.nav_add_appointment -> "Appointment"
                    else -> ""
                }
            }

            val itemId = when (category) {
                "Event" -> R.id.nav_add_event
                "Trip" -> R.id.nav_add_trip
                "Appointment" -> R.id.nav_add_appointment
                else -> null
            }
            itemId?.let {
                nav.menu.findItem(it)?.isChecked = true
                nav.menu.findItem(it)?.title = ""
            }
        }
    }

    private fun goHome(message: String = "") {
        if (message.isNotEmpty()) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("success_message", message)
        }
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav?.selectedItemId = R.id.eventListFragment
        findNavController().popBackStack(R.id.eventListFragment, false)
    }
}