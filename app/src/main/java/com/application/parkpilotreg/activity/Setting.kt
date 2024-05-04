package com.application.parkpilotreg.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.application.parkpilotreg.databinding.SettingBinding
import com.application.parkpilotreg.viewModel.ProfileViewModel

class Setting : AppCompatActivity() {

    private lateinit var binding: SettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = ProfileViewModel()

        viewModel.loadProfile(this, binding.imageViewProfilePicture, binding.textViewUserName)

        binding.buttonEditProfile.setOnClickListener {
            viewModel.personalInformation(this)
        }
        binding.textViewSpotDetails.setOnClickListener {
            viewModel.spotDetail(this)
        }
        binding.textViewLogout.setOnClickListener {
            viewModel.logout(this)
        }
    }
}