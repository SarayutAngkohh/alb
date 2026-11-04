package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.DestinationRepository
import com.example.util.PreferencesManager

class AlongTheBooksApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { DestinationRepository(database.destinationDao()) }
    val preferencesManager by lazy { PreferencesManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AlongTheBooksApp
            private set
    }
}
