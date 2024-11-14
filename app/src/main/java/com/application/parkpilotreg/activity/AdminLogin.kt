package com.application.parkpilotreg.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.parkpilotreg.User
import com.application.parkpilotreg.Utils
import com.application.parkpilotreg.databinding.AdminLoginPanelBinding
import com.application.parkpilotreg.viewModel.AdminLoginViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await


class AdminLogin : AppCompatActivity() {
    private lateinit var binding: AdminLoginPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminLoginPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminLoginViewModel() as T
            }
        })[AdminLoginViewModel::class.java]

        binding.buttonLogin.setOnClickListener {
            viewModel.signIn(
                email = binding.editTextEmail.text.toString(),
                password = binding.editTextPassword.text.toString(),
                onSuccess = {
                    val intent = Intent(this, Setting::class.java)
                    Utils.startActivityWithCleanStack(this, intent)
                },
                onFailed = {
                    Toast.makeText(
                        this,
                        "Login Failed",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onComplete = {

                }
            )
        }
    }
}