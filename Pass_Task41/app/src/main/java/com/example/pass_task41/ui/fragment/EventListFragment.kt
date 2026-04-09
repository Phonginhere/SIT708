package com.example.pass_task41.ui

import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pass_task41.R
import com.example.pass_task41.data.local.EventViewModel
import com.example.pass_task41.ui.theme.Pass_Task41Theme

class EventListFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Pass_Task41Theme {
                    val events by viewModel.upcomingEvents.collectAsState()

                    EventListScreen(
                        events = events,
                        onEditClick = { event ->
                            findNavController().navigate(R.id.action_list_to_edit, Bundle().apply {
                                putInt("eventId", event.id)
                                putString("category", event.category)
                            })
                        },
                        onDeleteClick = { event ->
                            viewModel.delete(event)
                            Toast.makeText(requireContext(), "Delete successful", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteMultiple = { eventList ->
                            eventList.forEach { viewModel.delete(it) }
                            Toast.makeText(requireContext(), "Deleted ${eventList.size} events", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}