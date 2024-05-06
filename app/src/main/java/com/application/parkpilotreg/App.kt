package com.application.parkpilotreg

import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import kotlin.properties.Delegates

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

object User {
    // if UID is null it means user not login yet
    lateinit var UID: String
}

object Utils {
    fun errorToast(context: Context, message: String) {
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        val inflate =
            (context as AppCompatActivity).layoutInflater.inflate(R.layout.toast, null).apply {
                findViewById<LinearLayout>(R.id.linearLayout).backgroundTintList =
                    ColorStateList.valueOf(Color.RED)
                findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.block_icon)
                findViewById<TextView>(R.id.textView).text = message
            }
        toast.view = inflate
        toast.show()
    }

    fun truthToast(context: Context, message: String) {
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        val inflate =
            (context as AppCompatActivity).layoutInflater.inflate(R.layout.toast, null).apply {
                findViewById<LinearLayout>(R.id.linearLayout).backgroundTintList =
                    ColorStateList.valueOf(Color.GREEN)
                findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.check_circle_icon)
                findViewById<TextView>(R.id.textView).text = message
            }
        toast.view = inflate
        toast.show()
    }
}