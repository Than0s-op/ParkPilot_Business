package com.application.parkpilotreg.module.firebase.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.application.parkpilotreg.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class GoogleSignIn : Activity() {

    // for logs
    private val TAG = "GoogleActivity"

    // for activity result tag
    private val RC_SIGN_IN = 9001

    // it will store firebase auth
    private lateinit var auth: FirebaseAuth

    // it will store google sign in client
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Auth with firebase auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // force user to select google account again
        googleSignInClient.revokeAccess()

        signIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)

                // read method name
                finishActivityWithResult(RESULT_CANCELED)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    // read method name
                    finishActivityWithResult(RESULT_OK)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    // read method name
                    finishActivityWithResult(RESULT_CANCELED)
                }
            }
    }

    private fun signIn() {
        // google sign intent
        val signInIntent = googleSignInClient.signInIntent

        // start activity for result along with tag to catch this intent result
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun finishActivityWithResult(resultStatus: Int) {
        // you can also return some data to previous activity using below two line code
        // val returnIntent = Intent().putExtra("","")
        // setResult(resultStatus,returnIntent)

        // setting result of this activity (success,failed)
        setResult(resultStatus)

        // finishing to this activity
        finish()
    }
}