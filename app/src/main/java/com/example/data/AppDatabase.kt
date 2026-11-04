package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DestinationDao
import com.example.data.model.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Destination::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "along_the_books_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate sample test stations
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).destinationDao()
                            dao.insert(
                                Destination(
                                    name = "สถานี BTS หมอชิต (สำหรับทดสอบ)",
                                    latitude = 13.8026,
                                    longitude = 100.5538,
                                    alertDistanceMeters = 300,
                                    category = "BTS"
                                )
                            )
                            dao.insert(
                                Destination(
                                    name = "สถานีกลางกรุงเทพอภิวัฒน์ (สำหรับทดสอบ)",
                                    latitude = 13.8042,
                                    longitude = 100.5401,
                                    alertDistanceMeters = 500,
                                    category = "รถไฟ"
                                )
                            )
                            dao.insert(
                                Destination(
                                    name = "สถานี BTS อโศก (สำหรับทดสอบ)",
                                    latitude = 13.7370,
                                    longitude = 100.5604,
                                    alertDistanceMeters = 300,
                                    category = "BTS"
                                )
                            )
                            dao.insert(
                                Destination(
                                    name = "สถานี BTS สยาม (สำหรับทดสอบ)",
                                    latitude = 13.7456,
                                    longitude = 100.5342,
                                    alertDistanceMeters = 300,
                                    category = "BTS"
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
