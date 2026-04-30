package com.example.p71.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.p71.R
import com.example.p71.ui.theme.P71Theme
import com.example.p71.viewmodel.ItemViewModel

class ItemsListFragment : Fragment() {

    private val viewModel: ItemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                P71Theme {
                    ItemsListScreen(
                        viewModel = viewModel,
                        onItemClick = { itemId ->
                            findNavController().navigate(
                                R.id.action_list_to_detail,
                                bundleOf("itemId" to itemId)
                            )
                        }
                    )
                }
            }
        }
    }
}