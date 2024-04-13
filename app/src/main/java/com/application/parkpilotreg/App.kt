package com.application.parkpilotreg

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import kotlin.properties.Delegates

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
object User{
    // if UID is null it means user not login yet
    lateinit var UID:String
}