package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Destination
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destinations ORDER BY id DESC")
    fun getAllDestinations(): Flow<List<Destination>>

    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: Long): Destination?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(destination: Destination): Long

    @Update
    suspend fun update(destination: Destination)

    @Delete
    suspend fun delete(destination: Destination)

    @Query("DELETE FROM destinations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
