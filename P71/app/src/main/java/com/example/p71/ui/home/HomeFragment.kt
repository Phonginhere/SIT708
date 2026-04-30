package com.example.p71.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.p71.R
import com.example.p71.ui.theme.P71Theme

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                P71Theme {
                    HomeScreen(
                        onCreateClick = {
                            findNavController().navigate(R.id.action_home_to_create)
                        },
                        onShowAllClick = {
                            findNavController().navigate(R.id.action_home_to_list)
                        }
                    )
                }
            }
        }
    }
}