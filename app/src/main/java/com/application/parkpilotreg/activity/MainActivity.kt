package com.application.parkpilotreg.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.application.parkpilotreg.User as appUser

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getting current user status

        // if User is signed in below block will execute (if current user is not null)
        if (Firebase.auth.currentUser?.uid != null) {

            // store UID in application layer
            appUser.UID = Firebase.auth.currentUser?.uid!!

            // start the registration activity
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java).apply {
                // clear the activity stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        // No user is signed in yet
        else {
            startActivity(Intent(this, AuthenticationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}