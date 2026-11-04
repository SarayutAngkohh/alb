package com.example.data.repository

import com.example.data.dao.DestinationDao
import com.example.data.model.Destination
import kotlinx.coroutines.flow.Flow

class DestinationRepository(private val destinationDao: DestinationDao) {
    val allDestinations: Flow<List<Destination>> = destinationDao.getAllDestinations()

    suspend fun getDestinationById(id: Long): Destination? {
        return destinationDao.getDestinationById(id)
    }

    suspend fun insert(destination: Destination): Long {
        return destinationDao.insert(destination)
    }

    suspend fun update(destination: Destination) {
        destinationDao.update(destination)
    }

    suspend fun delete(destination: Destination) {
        destinationDao.delete(destination)
    }

    suspend fun deleteById(id: Long) {
        destinationDao.deleteById(id)
    }
}
