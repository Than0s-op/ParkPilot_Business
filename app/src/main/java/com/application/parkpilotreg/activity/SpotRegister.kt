package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.application.parkpilotreg.R
import com.application.parkpilotreg.StationBasic
import com.application.parkpilotreg.Utils
import com.application.parkpilotreg.databinding.LocationPickerBinding
import com.application.parkpilotreg.databinding.SpotRegisterBinding
import com.application.parkpilotreg.viewModel.SpotRegisterViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.util.GeoPoint
import com.application.parkpilotreg.AccessHours as DataAccessHours
import com.application.parkpilotreg.StationAdvance as StationAdvance_DS

class SpotRegister : AppCompatActivity(R.layout.spot_register) {
    private lateinit var binding: SpotRegisterBinding
    private lateinit var viewModel: SpotRegisterViewModel
    private lateinit var bindingLocationPicker: LocationPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SpotRegisterBinding.inflate(layoutInflater, null, false)
        bindingLocationPicker = LocationPickerBinding.inflate(layoutInflater, null, false)
        viewModel = ViewModelProvider(this)[SpotRegisterViewModel::class.java]

        setContentView(binding.root)

        val imageViews = arrayOf(
            binding.imageView1,
            binding.imageView2,
            binding.imageView3
        )


        var openFlag = false


        viewModel.loadActivity {
            binding.shimmerLayout.shimmerLayout.visibility = View.GONE
            binding.linearLayout.visibility = View.VISIBLE
        }

        viewModel.init(this, bindingLocationPicker.mapView)

        binding.buttonLocationPick.setOnClickListener {
            showLocationPickerDialog()
        }

        binding.linearLayoutTimeShower.editTextOpenTime.setOnClickListener {
            openFlag = true
            viewModel.timePicker(supportFragmentManager, "open")
        }

        binding.linearLayoutTimeShower.editTextCloseTime.setOnClickListener {
            viewModel.timePicker(supportFragmentManager, "close")
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        var flagImageView1 = false
        imageViews[0].setOnClickListener {
            if (viewModel.imageViewsUri[0] != null) {
                imageViews[0].load(R.drawable.add_icon)
                viewModel.imageViewsUri[0] = null
            } else {
                flagImageView1 = true
                viewModel.imagePicker()
            }
        }

        var flagImageView2 = false
        imageViews[1].setOnClickListener {
            if (viewModel.imageViewsUri[1] != null) {
                imageViews[1].load(R.drawable.add_icon)
                viewModel.imageViewsUri[1] = null
            } else {
                flagImageView2 = true
                viewModel.imagePicker()
            }
        }

        imageViews[2].setOnClickListener {
            if (viewModel.imageViewsUri[2] != null) {
                imageViews[2].load(R.drawable.add_icon)
                viewModel.imageViewsUri[2] = null
            } else {
                viewModel.imagePicker()
            }
        }

        // when user will type in search bar and press search(action) button (present on keyboard)
        bindingLocationPicker.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                bindingLocationPicker.progressBarSearch.visibility = View.VISIBLE
                viewModel.search(
                    this@SpotRegister,
                    bindingLocationPicker.searchView.query.toString()
                ) {
                    bindingLocationPicker.progressBarSearch.visibility = View.GONE
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        // when current location button press
        bindingLocationPicker.buttonCurrentLocation.setOnClickListener {
            // it will set current location in mapView
            viewModel.getCurrentLocation(this)
        }

        binding.buttonSubmit.setOnClickListener {
            var isValid = true
            if (isNameInvalid(binding.editTextStationName.text.toString())) {
                binding.editTextStationName.error = "Must contain only [A-z] [0-9]"
                isValid = false
            }
            if (isNameInvalid(binding.editTextPolicies.text.toString())) {
                binding.editTextPolicies.error = "Must contain only [A-z] [0-9]"
                isValid = false
            }
            if (isNumberInvalid(binding.editTextStartingPrice.text.toString())) {
                binding.editTextStartingPrice.error = "Must contain only [0-9]"
                isValid = false
            }
            if (isNumberInvalid(binding.editTextReservedSpots.text.toString())) {
                binding.editTextStartingPrice.error = "Must contain only [0-9]"
                isValid = false
            }

            for (i in 0..<3) {
                if (viewModel.imageViewsUri[i] == null) {
                    Toast.makeText(this, "All images must selected", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            if (isValid) {
                showProgress()
                viewModel.uploadDetails(
                    this,
                    StationBasic(
                        binding.editTextStationName.text.toString(),
                        binding.editTextStartingPrice.text.toString().toInt(),
                        binding.editTextReservedSpots.text.toString().toInt()
                    ),

                    StationAdvance_DS(
                        binding.editTextPolicies.text.toString(),
                        getAmenities(),
                        getAccessTime()
                    )
                )
            }
        }

        viewModel.timePicker.liveDataTimePicker.observe(this) {
            if (openFlag) {
                binding.linearLayoutTimeShower.editTextOpenTime.setText(
                    viewModel.timePicker.format12(
                        it
                    )
                )
                openFlag = false
            } else {
                binding.linearLayoutTimeShower.editTextCloseTime.setText(
                    viewModel.timePicker.format12(
                        it
                    )
                )
            }
        }

        viewModel.photoPicker.pickedImage.observe(this) {
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

        viewModel.liveDataStationBasic.observe(this) {
            it?.let {
                binding.editTextStationName.setText(it.name)
                binding.editTextStartingPrice.setText(it.price.toString())
                binding.editTextReservedSpots.setText(it.reserved.toString())
            }
        }
        viewModel.liveDataStationAdvance.observe(this) {
            it?.let {
                binding.editTextPolicies.setText(it.policies)
                binding.linearLayoutTimeShower.editTextOpenTime.setText(it.accessHours.open)
                binding.linearLayoutTimeShower.editTextCloseTime.setText(it.accessHours.close)
                loadDaysSwitch(it.accessHours.selectedDays)
                loadAmenities(it.amenities)
            }
        }
        viewModel.liveDataStationLocation.observe(this) {
            it?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                viewModel.fillAddress(this, binding.editTextAddress, geoPoint)
                viewModel.setMarker(geoPoint)
            }
        }
        viewModel.liveDataImages.observe(this) {
            for (i in it.indices) {
                imageViews[i].load(it[i])
                viewModel.imageViewsUri[i] = it[i]
            }
        }
        viewModel.isUploaded.observe(this) { isUploaded ->
            unShowProgress()
            if (isUploaded) {
                Utils.truthToast(this, "Information Save Successfully")
                finish()
            } else {
                Utils.errorToast(this, "Failed Save Information")
            }
        }
    }

    private fun getAmenities(): List<String> {
        val selectedAmenities: ArrayList<String> = ArrayList()
        selectedAmenities.apply {
            for (id in binding.chipGroupAmenities.chipGroupAmenities.checkedChipIds) {
                when (id) {
                    R.id.chipEvCharging -> add(getString(R.string.ev_charging))
                    R.id.chipValet -> add(getString(R.string.valet))
                    R.id.chipGarage -> add(getString(R.string.garage))
                    R.id.chipStaff -> add(getString(R.string.on_site_staff))
                    R.id.chipWheelchair -> add(getString(R.string.wheelchair_accessible))
                }
            }
        }
        return selectedAmenities
    }

    private fun loadAmenities(list: List<String>) {
        for (i in list) {
            when (i) {
                getString(R.string.ev_charging) -> (binding.chipGroupAmenities.chipGroupAmenities[0] as Chip).isChecked =
                    true

                getString(R.string.valet) -> (binding.chipGroupAmenities.chipGroupAmenities[1] as Chip).isChecked =
                    true

                getString(R.string.garage) -> (binding.chipGroupAmenities.chipGroupAmenities[2] as Chip).isChecked =
                    true

                getString(R.string.on_site_staff) -> (binding.chipGroupAmenities.chipGroupAmenities[3] as Chip).isChecked =
                    true

                getString(R.string.wheelchair_accessible) -> (binding.chipGroupAmenities.chipGroupAmenities[4] as Chip).isChecked =
                    true
            }
        }
    }

    private fun getAccessTime(): DataAccessHours {
        val selectedDays: ArrayList<String> = ArrayList()

        selectedDays.apply {
            for (id in binding.chipGroupDays.chipGroupDays.checkedChipIds) {
                when (id) {
                    R.id.chipMonday -> add(getString(R.string.monday))
                    R.id.chipTuesday -> add(getString(R.string.tuesday))
                    R.id.chipWednesday -> add(getString(R.string.wednesday))
                    R.id.chipThursday -> add(getString(R.string.thursday))
                    R.id.chipFriday -> add(getString(R.string.friday))
                    R.id.chipSaturday -> add(getString(R.string.saturday))
                    R.id.chipSunday -> add(getString(R.string.sunday))
                }
            }
        }

        return DataAccessHours(
            binding.linearLayoutTimeShower.editTextOpenTime.text.toString(),
            binding.linearLayoutTimeShower.editTextCloseTime.text.toString(),
            selectedDays
        )
    }

    private fun showProgress() {
        // show progress bar
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSubmit.visibility = View.GONE

        // to disable user interaction with ui
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun unShowProgress() {
        // hide progress bar
        binding.progressBar.visibility = View.GONE
        binding.buttonSubmit.visibility = View.VISIBLE

        // to enable user interaction with ui
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun isNameInvalid(name: String): Boolean {
        val pattern = Regex("[A-Za-z0-9\\s\n]+")
        return !pattern.matches(name)
    }

    private fun isNumberInvalid(number: String): Boolean {
        val pattern = Regex("[0-9]+")
        return !pattern.matches(number)
    }

    private fun loadDaysSwitch(list: List<String>) {
        for (i in list) {
            when (i) {
                getString(R.string.monday) -> (binding.chipGroupDays.chipGroupDays[0] as Chip).isChecked =
                    true

                getString(R.string.tuesday) -> (binding.chipGroupDays.chipGroupDays[1] as Chip).isChecked =
                    true

                getString(R.string.wednesday) -> (binding.chipGroupDays.chipGroupDays[2] as Chip).isChecked =
                    true

                getString(R.string.thursday) -> (binding.chipGroupDays.chipGroupDays[3] as Chip).isChecked =
                    true

                getString(R.string.friday) -> (binding.chipGroupDays.chipGroupDays[4] as Chip).isChecked =
                    true

                getString(R.string.saturday) -> (binding.chipGroupDays.chipGroupDays[5] as Chip).isChecked =
                    true

                getString(R.string.sunday) -> (binding.chipGroupDays.chipGroupDays[6] as Chip).isChecked =
                    true
            }
        }
    }

    private fun showLocationPickerDialog() {
        val input = bindingLocationPicker.root

        if (input.parent != null) (input.parent as ViewGroup).removeView(input)
        MaterialAlertDialogBuilder(this)
            .setView(input)
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Ok") { dialog, _ ->
                viewModel.fillAddress(this, binding.editTextAddress, viewModel.marker.position)
                dialog.dismiss()
            }
            .show()
    }
}