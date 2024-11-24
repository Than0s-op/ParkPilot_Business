package com.application.parkpilotreg.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.application.parkpilotreg.module.firebase.database.UserBasic
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.application.parkpilotreg.User as appUser

class Main : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getting current user status

        // if User is signed in below block will execute (if current user is not null)
        if (Firebase.auth.currentUser?.uid != null) {

            // store UID in application layer
//            appUser.UID = Firebase.auth.currentUser?.uid!!

            // start the registration activity
            startActivity(Intent(this@Main, Setting::class.java).apply {
                // clear the activity stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        // No user is signed in yet
        else {
            startActivity(Intent(this, Authentication::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            CoroutineScope(Dispatchers.IO).launch {
                if (UserBasic().getProfile(appUser.UID) != null) {
                    startActivity(Intent(this@Main, Setting::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else finish()
            }
        }
    }
}
