package com.application.parkpilotreg.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.application.parkpilotreg.User
import com.application.parkpilotreg.databinding.SettingBinding
import com.application.parkpilotreg.viewModel.ProfileViewModel

class Setting : AppCompatActivity() {

    private lateinit var binding: SettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadViews()

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
        binding.textViewAddFreeSpot.setOnClickListener {
            startActivity(Intent(this, AddFreeSpot::class.java))
        }
        binding.textViewFreeSpotList.setOnClickListener {
            startActivity(Intent(this, FreeSpotList::class.java))
        }
    }

    private fun loadViews() {
        when (User.isAdmin) {
            true -> {
                binding.textViewAddFreeSpot.visibility = View.VISIBLE
                binding.textViewFreeSpotList.visibility = View.VISIBLE
                binding.textViewUserName.text = "Admin"
            }

            false -> {
                binding.textViewSpotDetails.visibility = View.VISIBLE
                binding.buttonEditProfile.visibility = View.VISIBLE
            }
        }
    }
}