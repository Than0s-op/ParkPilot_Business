package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.R
import com.application.parkpilotreg.databinding.AddFreeSpotBinding
import com.application.parkpilotreg.databinding.LocationPickerBinding
import com.application.parkpilotreg.module.PhotoPicker
import com.application.parkpilotreg.viewModel.AddFreeSpotViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.GeoPoint

class AddFreeSpot : AppCompatActivity() {
    private lateinit var binding: AddFreeSpotBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddFreeSpotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var id: String? = null
        val viewModel = AddFreeSpotViewModel()
        val imagePicker by lazy { PhotoPicker(this) }
        val bindingLocationPicker = LocationPickerBinding.inflate(layoutInflater)
        val dialogBox =
            MaterialAlertDialogBuilder(this).setView(bindingLocationPicker.root).create()
        viewModel.init(this, bindingLocationPicker.mapView)

        intent.getStringExtra("id")?.let {
            showShimmer()
            viewModel.getFreeSpot(it, onComplete = {
                hideShimmer()
            })
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        val imageViews = arrayOf(
            binding.imageView1,
            binding.imageView2,
            binding.imageView3
        )

        var flagImageView1 = false
        imageViews[0].setOnClickListener {
            if (viewModel.imageViewsUri[0] != null) {
                imageViews[0].load(R.drawable.add_icon)
                viewModel.imageViewsUri[0] = null
            } else {
                flagImageView1 = true
                imagePicker.showPhotoPicker()
            }
        }

        var flagImageView2 = false
        imageViews[1].setOnClickListener {
            if (viewModel.imageViewsUri[1] != null) {
                imageViews[1].load(R.drawable.add_icon)
                viewModel.imageViewsUri[1] = null
            } else {
                flagImageView2 = true
                imagePicker.showPhotoPicker()
            }
        }

        imageViews[2].setOnClickListener {
            if (viewModel.imageViewsUri[2] != null) {
                imageViews[2].load(R.drawable.add_icon)
                viewModel.imageViewsUri[2] = null
            } else {
                imagePicker.showPhotoPicker()
            }
        }

        binding.buttonAdd.setOnClickListener {
            showProgressBar()
            viewModel.set(
                spot = FreeSpot(
                    id = id ?: System.currentTimeMillis().toString(),
                    landMark = binding.editTextLandMark.text.toString(),
                    policies = binding.editTextPolicies.text.toString(),
                    location = viewModel.marker.position.let {
                        GeoPoint(it.latitude, it.longitude)
                    },
                    images = viewModel.imageViewsUri.toList()
                ),
                onSuccess = {
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                    finish()
                },
                onFailed = {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                },
                onComplete = {
                    hideProgressBar()
                }
            )
        }

        binding.buttonLocationPick.setOnClickListener {
            dialogBox.show()
        }

        dialogBox.setOnDismissListener {
            viewModel.fillAddress(this, binding.editTextAddress, viewModel.marker.position)
        }

        imagePicker.pickedImage.observe(this) {
            if (flagImageView1) {
                viewModel.imageViewsUri[0] = it
                imageViews[0].load(it)
                flagImageView1 = false
            } else if (flagImageView2) {
                viewModel.imageViewsUri[1] = it
                imageViews[1].load(it)
                flagImageView2 = false
            } else {
                viewModel.imageViewsUri[2] = it
                imageViews[2].load(it)
            }
        }

        viewModel.freeSpot.observe(this) {
            id = it.id
            binding.editTextLandMark.setText(it.landMark)
            binding.editTextPolicies.setText(it.policies)
            viewModel.setMarker(
                org.osmdroid.util.GeoPoint(
                    it.location.latitude,
                    it.location.longitude
                )
            )
            viewModel.fillAddress(this, binding.editTextAddress, viewModel.marker.position)
            it.images.forEachIndexed { index, uri ->
                imageViews[index].load(uri)
                viewModel.imageViewsUri[index] = uri
            }
        }

        // when user will type in search bar and press search(action) button (present on keyboard)
        bindingLocationPicker.searchView.editText.setOnEditorActionListener { _, _, _ ->

            // hide the searchView(search suggestion box)
            bindingLocationPicker.searchView.hide()

            // creating co-routine scope to run search method
            viewModel.search(this, bindingLocationPicker.searchView.text.toString())
            false
        }

        // when current location button press
        bindingLocationPicker.buttonCurrentLocation.setOnClickListener {
            // it will set current location in mapView
            viewModel.getCurrentLocation(this)
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showShimmer() {
        binding.shimmerLayout.root.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.root.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
    }
}