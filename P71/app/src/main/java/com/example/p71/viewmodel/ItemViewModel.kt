package com.example.p71.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.p71.data.local.Item
import com.example.p71.data.local.ItemDatabase
import com.example.p71.data.local.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ItemRepository
    val allItems: StateFlow<List<Item>>

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val categories = listOf("Electronics", "Pets", "Wallets")

    init {
        val dao = ItemDatabase.getDatabase(application).itemDao()
        repository = ItemRepository(dao)

        allItems = _selectedCategory
            .flatMapLatest { category ->
                if (category == null) {
                    repository.getAllItems()
                } else {
                    repository.getItemsByCategory(category)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun insertItem(
        postType: String,
        name: String,
        phone: String,
        description: String,
        category: String,
        date: String,
        location: String,
        imageData: ByteArray?
    ) {
        viewModelScope.launch {
            repository.insert(
                Item(
                    postType = postType,
                    name = name,
                    phone = phone,
                    description = description,
                    category = category,
                    date = date,
                    location = location,
                    imageData = imageData
                )
            )
        }
    }

    suspend fun getItemById(id: Int): Item? = repository.getItemById(id)

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}