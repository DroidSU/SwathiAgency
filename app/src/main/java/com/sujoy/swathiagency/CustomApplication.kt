package com.sujoy.swathiagency

import android.app.Application
import com.google.firebase.FirebaseApp
import com.sujoy.swathiagency.database.AppDatabase

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val database = AppDatabase.getDatabase(this)
    }
}