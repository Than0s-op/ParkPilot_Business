package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.parkpilotreg.R
import com.application.parkpilotreg.viewModel.AuthenticationViewModel
import com.chaos.view.PinView
import com.hbb20.CountryCodePicker


class Authentication : AppCompatActivity(R.layout.authentication) {
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init views
        val editTextPhoneNumber: EditText = findViewById(R.id.editTextPhoneNumber)
        val buttonVerifyPhoneNumber: Button = findViewById(R.id.buttonVerifyPhoneNumber)
        progressBar = findViewById(R.id.progressBar)
        val scrollViewLogin: ScrollView = findViewById(R.id.scrollViewLogin)
        val scrollViewOTP: ScrollView = findViewById(R.id.scrollViewOTP)
        val pinViewOTP: PinView = findViewById(R.id.pinViewOTP)
        val buttonOTPVerification: Button = findViewById(R.id.buttonOTPVerification)
        val countryCodePicker: CountryCodePicker = findViewById(R.id.countryCodePicker)
        val textViewNumber: TextView = findViewById(R.id.textViewNumber)
        val buttonResendOTP: Button = findViewById(R.id.buttonResendOTP)

        findViewById<RelativeLayout>(R.id.divider1).findViewById<TextView>(R.id.dividerTextView).text =
            "Log in or sign up"

        findViewById<RelativeLayout>(R.id.divider2).findViewById<TextView>(R.id.dividerTextView).text =
            "or"

        // getting authentication view model reference [init]
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthenticationViewModel(this@Authentication) as T
            }
        })[AuthenticationViewModel::class.java]

        // [init]
        // setting phone number to (We have sent a verification code to) text view
        // [we are setting here because of reconfiguration, if activity will regenerate we will set phone number to that view again]
        textViewNumber.text = viewModel.dashSeparate(viewModel.phoneNumberWithCountryCode)

//      .......... [ phone auth ] ................


        // setting visibility as according to view model [init]
        scrollViewLogin.visibility = viewModel.scrollViewLoginVisibility
        scrollViewOTP.visibility = viewModel.scrollViewOTPVisibility


        buttonVerifyPhoneNumber.setOnClickListener { _ ->

            // mobile number validation
            if (editTextPhoneNumber.text.length != 10) {
                editTextPhoneNumber.error = "Invalid number"
                return@setOnClickListener
            } else {
                editTextPhoneNumber.error = null
            }

            // progress bar
            showProgress()

            // storing user number with country code
            viewModel.phoneNumberWithCountryCode =
                countryCodePicker.selectedCountryCodeWithPlus + editTextPhoneNumber.text.toString()

            // set phone number with country code to OTP view's message (We have sent a verification code to)
            textViewNumber.text = viewModel.dashSeparate(viewModel.phoneNumberWithCountryCode)

            // start verification
            viewModel.sendVerificationCode()
        }

        // view model verification Id observer, It will be react when verification Id will change
        viewModel.verificationCode.observe(this) { verificationCode ->

            // If OTP send successfully or unsuccessfully, then hide progress bar
            unShowProgress()

            // when "verificationId == null" OTP send to failed,
            // otherwise OTP send successfully

            // if OTP send successfully
            if (verificationCode.isNotEmpty()) {
                // hide login view and
                View.GONE.let {
                    scrollViewLogin.visibility = it
                    viewModel.scrollViewLoginVisibility = it
                }

                // show OTP view
                View.VISIBLE.let {
                    scrollViewOTP.visibility = it
                    viewModel.scrollViewOTPVisibility = it
                }

                // show successful toast
                Toast.makeText(this, "OTP Send Successfully", Toast.LENGTH_SHORT).show()
            }
            // if OTP not send successfully (error)
            else {
                // show failed toast
                Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show()
            }
        }

        buttonOTPVerification.setOnClickListener { _ ->
            showProgress()

            // pass user entered OTP to check entered OTP correct or not
            viewModel.verifyPhoneNumberWithCode(
                pinViewOTP.text.toString()
            )
        }

        // It will be react when we get result of the "verifyPhoneNumberWithCode" function call
        viewModel.verifyPhoneNumberWithCodeResult.observe(this) { isCorrect ->
            unShowProgress()
            // when Credential match (OTP is correct)
            if (isCorrect) {
                // show successful toast
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()

                // start the next activity
                viewModel.startNextActivity(this)
            } else {
                // clear OTP box
                pinViewOTP.setText("")

                // disable OTP view and
                View.GONE.let {
                    scrollViewOTP.visibility = it
                    viewModel.scrollViewOTPVisibility = it
                }

                // show login view again
                View.VISIBLE.let {
                    scrollViewLogin.visibility = it
                    viewModel.scrollViewLoginVisibility = it
                }

                // clear phone number text view
                editTextPhoneNumber.setText("")

                // show failed toast
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        buttonResendOTP.setOnClickListener { _ ->
            // resend verification code request
            viewModel.resendVerificationCode()
        }

//      .......... [ google singIn ] ................

        // initializing google sign in button
        val buttonGoogleSignIn: Button = findViewById(R.id.buttonGoogleLogin)

        buttonGoogleSignIn.setOnClickListener { _ ->
            // start the google sign in intent
            viewModel.startGoogleSignInIntent(this)
        }

        viewModel.googleSignInResult.observe(this) { isOk ->
            // successfully get google account of user
            if (isOk) {
                viewModel.startNextActivity(this)
            }
            // otherwise
            else {
                Toast.makeText(this, "Failed to Login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgress() {
        // show progress bar
        progressBar.visibility = View.VISIBLE

        // to disable user interaction with ui
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun unShowProgress() {
        // hide progress bar
        progressBar.visibility = View.GONE

        // to enable user interaction with ui
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}