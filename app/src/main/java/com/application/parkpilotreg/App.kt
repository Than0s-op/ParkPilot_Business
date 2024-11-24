package com.application.parkpilotreg

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
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
    val UID get() = Firebase.auth.uid!!
    val isAdmin get() = Firebase.auth.currentUser?.email == "thanosop150@gmail.com"
}

object Utils {

    fun isLocalUri(uri: Uri): Boolean {
        return uri.scheme == "file" || uri.scheme == "content" || uri.scheme == "android.resource"
    }

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

    fun startActivityWithCleanStack(context: Context, intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}