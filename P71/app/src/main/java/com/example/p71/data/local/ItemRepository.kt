package com.example.p71.data.local

import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()

    fun getItemsByCategory(category: String): Flow<List<Item>> =
        itemDao.getItemsByCategory(category)

    suspend fun getItemById(id: Int): Item? = itemDao.getItemById(id)

    suspend fun insert(item: Item) = itemDao.insert(item)

    suspend fun deleteById(id: Int) = itemDao.deleteById(id)
}