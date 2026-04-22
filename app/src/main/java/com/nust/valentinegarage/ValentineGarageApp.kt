package com.nust.valentinegarage

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ValentineGarageApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable Firestore offline persistence for zero-latency
        // operations in dead zones (under trucks, back warehouse, etc.)
        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }
}
