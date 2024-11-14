package com.application.parkpilotreg.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.parkpilotreg.User
import com.application.parkpilotreg.Utils
import com.application.parkpilotreg.databinding.AdminLoginPanelBinding
import com.application.parkpilotreg.viewModel.AdminLoginViewModel


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

        binding.divider1.dividerTextView.text = "Admin Login"

        binding.buttonLogin.setOnClickListener {
            showProgress()
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
                    hideProgress()
                }
            )
        }
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonLogin.visibility = View.GONE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.buttonLogin.visibility = View.VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}