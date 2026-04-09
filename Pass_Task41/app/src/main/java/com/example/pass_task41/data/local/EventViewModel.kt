package com.example.pass_task41.data.local

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository = EventRepository(EventDatabase.getDatabase(application).eventDao())

    val upcomingEvents: StateFlow<List<Event>> =
        repository.upcomingEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

//    init {
//        val dao = EventDatabase.getDatabase(application).eventDao()
//        repository = EventRepository(dao)
//    }

    suspend fun getById(id: Int): Event? = repository.getById(id)

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)
    }

    fun update(event: Event) = viewModelScope.launch {
        repository.update(event)
    }

    fun delete(event: Event) = viewModelScope.launch {
        repository.delete(event)
    }

}