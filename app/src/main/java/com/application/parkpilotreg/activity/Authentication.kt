package com.application.parkpilotreg.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.parkpilotreg.R
import com.application.parkpilotreg.Utils
import com.application.parkpilotreg.databinding.AuthenticationBinding
import com.application.parkpilotreg.viewModel.AuthenticationViewModel


class Authentication : AppCompatActivity() {
    private lateinit var binding: AuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.divider1.dividerTextView.text = "Log in or sign up"

        binding.divider2.dividerTextView.text = "or"

        // getting authentication view model reference [init]
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthenticationViewModel(this@Authentication) as T
            }
        })[AuthenticationViewModel::class.java]

        // [init]
        // setting phone number to (We have sent a verification code to) text view
        // [we are setting here because of reconfiguration, if activity will regenerate we will set phone number to that view again]
        binding.textViewNumber.text = viewModel.dashSeparate(viewModel.phoneNumberWithCountryCode)

//      .......... [ phone auth ] ................


        // setting visibility as according to view model [init]
        binding.scrollViewLogin.visibility = viewModel.scrollViewLoginVisibility
        binding.scrollViewOTP.visibility = viewModel.scrollViewOTPVisibility


        binding.buttonVerifyPhoneNumber.setOnClickListener { _ ->

            // mobile number validation
            if (isInvalidPhoneNumber(binding.editTextPhoneNumber.text.toString())) {
                binding.editTextPhoneNumber.error = "Invalid number"
                return@setOnClickListener
            } else {
                binding.editTextPhoneNumber.error = null
            }

            // progress bar
            showProgress()

            // storing user number with country code
            viewModel.phoneNumberWithCountryCode =
                binding.countryCodePicker.selectedCountryCodeWithPlus + binding.editTextPhoneNumber.text.toString()

            // set phone number with country code to OTP view's message (We have sent a verification code to)
            binding.textViewNumber.text =
                viewModel.dashSeparate(viewModel.phoneNumberWithCountryCode)

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
                    binding.scrollViewLogin.visibility = it
                    viewModel.scrollViewLoginVisibility = it
                }

                // show OTP view
                View.VISIBLE.let {
                    binding.scrollViewOTP.visibility = it
                    viewModel.scrollViewOTPVisibility = it
                }

                // show successful toast
                Utils.truthToast(this, "OTP Send Successfully")
            }
            // if OTP not send successfully (error)
            else {
                // show failed toast
                Utils.errorToast(this, "Failed to send OTP")
            }
        }

        binding.buttonOTPVerification.setOnClickListener { _ ->
            verifyOtpShowProcess()

            // pass user entered OTP to check entered OTP correct or not
            viewModel.verifyPhoneNumberWithCode(
                binding.pinViewOTP.text.toString()
            )
        }

        // It will be react when we get result of the "verifyPhoneNumberWithCode" function call
        viewModel.verifyPhoneNumberWithCodeResult.observe(this) { isCorrect ->
            verifyOtpHideProcess()
            // when Credential match (OTP is correct)
            if (isCorrect) {
                // show successful toast
                Utils.truthToast(this, "Login Successfully")

                // start the next activity
                viewModel.startNextActivity(this)
            } else {
                // clear OTP box
                binding.pinViewOTP.setText("")

                // disable OTP view and
                View.GONE.let {
                    binding.scrollViewOTP.visibility = it
                    viewModel.scrollViewOTPVisibility = it
                }

                // show login view again
                View.VISIBLE.let {
                    binding.scrollViewLogin.visibility = it
                    viewModel.scrollViewLoginVisibility = it
                }

                // clear phone number text view
                binding.editTextPhoneNumber.setText("")

                // show failed toast
                Utils.errorToast(this, "Invalid OTP")
            }
        }

        binding.buttonResendOTP.setOnClickListener { _ ->
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
                Utils.errorToast(this, "Failed to Login")
            }
        }

        val buttonAdminLogin: Button = findViewById(R.id.buttonAdminLogin)
        buttonAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLogin::class.java))
        }
    }

    private fun showProgress() {
        // show progress bar
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonVerifyPhoneNumber.visibility = View.GONE

        // to disable user interaction with ui
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun unShowProgress() {
        // hide progress bar
        binding.progressBar.visibility = View.GONE
        binding.buttonVerifyPhoneNumber.visibility = View.VISIBLE

        // to enable user interaction with ui
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun verifyOtpShowProcess() {
        binding.progressBarOtpVerification.visibility = View.VISIBLE
        binding.buttonOTPVerification.visibility = View.GONE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun verifyOtpHideProcess() {
        binding.progressBarOtpVerification.visibility = View.GONE
        binding.buttonOTPVerification.visibility = View.VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun isInvalidPhoneNumber(number: String): Boolean {
        val pattern = Regex("[0-9]{10}")
        return !pattern.matches(number)
    }
}