package com.application.parkpilotreg.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class AdminLoginViewModel : ViewModel() {
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onFailed: () -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {
                    onFailed()
                }.addOnCompleteListener {
                    onComplete()
                }
        } catch (_: Exception) {
            onFailed()
            onComplete()
        }
    }
}