package com.example.pass_task41.data.local

import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: EventDao) {

    val upcomingEvents: Flow<List<Event>> = dao.getUpcomingEvents()

    suspend fun getById(id: Int) = dao.getById(id)

    suspend fun insert(event: Event) = dao.insert(event)

    suspend fun update(event: Event) = dao.update(event)

    suspend fun delete(event: Event) = dao.delete(event)

}