package com.application.parkpilotreg.viewModel

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.parkpilotreg.R
import com.application.parkpilotreg.User
import com.application.parkpilotreg.activity.AuthenticationActivity
import com.application.parkpilotreg.activity.MainActivity
import com.application.parkpilotreg.activity.ParkRegisterActivity
import com.application.parkpilotreg.activity.UserRegisterActivity
import com.application.parkpilotreg.module.PhotoLoader
import com.application.parkpilotreg.module.firebase.Storage
import com.application.parkpilotreg.module.firebase.database.UserBasic
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    fun login(context: Context) {
        context.startActivity(Intent(context, AuthenticationActivity::class.java))
    }

    fun logout(context: Context) {
        // sign out the user
        Firebase.auth.signOut()
        Toast.makeText(context, "Logout Successfully", Toast.LENGTH_SHORT).show()

        // creating the intent of Authentication activity
        val intent = Intent(context, MainActivity::class.java).apply {
            // to clear activity stack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // start the activity
        context.startActivity(intent)
    }

    fun personalInformation(context: Context) {
        context.startActivity(Intent(context, UserRegisterActivity::class.java))
    }

    fun spotDetail(context: Context) {
        context.startActivity(Intent(context, ParkRegisterActivity::class.java))
    }

    fun loadProfile(context: Context, profileImage: ImageView, profileName: TextView) {
        Firebase.auth.currentUser?.let {
            viewModelScope.launch {
                profileImage.setImageDrawable(
                    PhotoLoader().getImage(
                        context,
                        Storage().userProfilePhotoGet(User.UID) ?: R.drawable.person_icon
                    ).drawable
                )
                profileName.text = UserBasic().getProfile(User.UID)?.userName ?: "User"
            }
        }
    }

}