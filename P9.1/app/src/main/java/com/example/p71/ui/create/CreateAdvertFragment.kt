package com.example.p71.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.p71.ui.theme.P71Theme
import com.example.p71.viewmodel.ItemViewModel

class CreateAdvertFragment : Fragment() {

    private val viewModel: ItemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                P71Theme {
                    CreateAdvertScreen(
                        viewModel = viewModel,
                        onSaveSuccess = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}