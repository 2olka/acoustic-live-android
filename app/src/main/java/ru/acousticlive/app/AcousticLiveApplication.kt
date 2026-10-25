package ru.acousticlive.app

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class AcousticLiveApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance(
            "https://acoustic-live-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).setPersistenceEnabled(true)
    }
}

