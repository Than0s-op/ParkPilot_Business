package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.application.parkpilotreg.R
import com.application.parkpilotreg.User
import com.application.parkpilotreg.viewModel.ProfileViewModel

class ProfileActivity : AppCompatActivity(R.layout.profile) {

    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var textViewUserName: TextView
    private lateinit var textViewPersonalInformation: TextView
    private lateinit var textViewSpotDetail: TextView
    private lateinit var textViewLogin: TextView
    private lateinit var textViewLogout: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture)
        textViewUserName = findViewById(R.id.textViewUserName)
        textViewPersonalInformation = findViewById(R.id.textViewPersonalInformation)
        textViewSpotDetail = findViewById(R.id.textViewSpotDetail)
        textViewLogin = findViewById(R.id.textViewLogin)
        textViewLogout = findViewById(R.id.textViewLogout)

        loadViews()

        val viewModel = ProfileViewModel()

        viewModel.loadProfile(this,imageViewProfilePicture,textViewUserName)

        textViewPersonalInformation.setOnClickListener{
            viewModel.personalInformation(this)
        }
        textViewSpotDetail.setOnClickListener {
            viewModel.spotDetail(this)
        }
        textViewLogout.setOnClickListener {
            viewModel.logout(this)
        }
        textViewLogin.setOnClickListener {
            viewModel.login(this)
        }
    }

    private fun loadViews() {
        textViewLogin.visibility = View.GONE
    }
}