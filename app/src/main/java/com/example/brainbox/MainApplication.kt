package com.example.brainbox

import android.app.Application
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase at the application level
        FirebaseApp.initializeApp(this)
    }
}