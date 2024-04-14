package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.application.parkpilotreg.R
import com.application.parkpilotreg.StationBasic
import com.application.parkpilotreg.viewModel.ParkRegisterViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.application.parkpilotreg.AccessHours as DataAccessHours
import com.application.parkpilotreg.StationAdvance as StationAdvance_DS

class ParkRegisterActivity : AppCompatActivity(R.layout.park_register) {
    private lateinit var chipGroupDays: ChipGroup
    private lateinit var chipGroupAmenities: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var editTextPolicies: EditText
    private lateinit var editTextOpenTime: EditText
    private lateinit var editTextCloseTime: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editTextStationName: EditText = findViewById(R.id.editTextStationName)
        val buttonLocationPick: Button = findViewById(R.id.buttonLocationPick)
        val editTextAddress: EditText = findViewById(R.id.editTextAddress)
        val buttonSubmit: Button = findViewById(R.id.buttonSubmit)
        val editTextStartingPrice: EditText = findViewById(R.id.editTextStartingPrice)
        editTextOpenTime = findViewById(R.id.editTextOpenTime)
        editTextCloseTime = findViewById(R.id.editTextCloseTime)
        editTextPolicies = findViewById(R.id.editTextPolicies)
        chipGroupDays = findViewById(R.id.chipGroupDays)
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities)
        progressBar = findViewById(R.id.progressBar)

        val imageViews = arrayOf<ImageView>(
            findViewById(R.id.imageView1),
            findViewById(R.id.imageView2),
            findViewById(R.id.imageView3)
        )

        val layoutLocationPicker = layoutInflater.inflate(R.layout.location_picker, null, false)

        val searchView: SearchView = layoutLocationPicker.findViewById(R.id.searchView)!!
        val searchBar: SearchBar = layoutLocationPicker.findViewById(R.id.searchBar)!!
        val mapView: MapView = layoutLocationPicker.findViewById(R.id.mapViewOSM)!!
        val buttonCurrentLocation: Button =
            layoutLocationPicker.findViewById(R.id.buttonCurrentLocation)!!
        val dialogBox = MaterialAlertDialogBuilder(this).setView(layoutLocationPicker).create()


        var openFlag = false

        val viewModel = ViewModelProvider(this)[ParkRegisterViewModel::class.java]

        viewModel.loadActivity()

        viewModel.init(this, mapView)

        buttonLocationPick.setOnClickListener {
            dialogBox.show()
        }

        dialogBox.setOnDismissListener {
            viewModel.fillAddress(this, editTextAddress, viewModel.marker.position)
        }

        editTextOpenTime.setOnClickListener {
            openFlag = true
            viewModel.timePicker(supportFragmentManager, "open")
        }

        editTextCloseTime.setOnClickListener {
            viewModel.timePicker(supportFragmentManager, "close")
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
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            // setting the typed text to the search bar
            searchBar.setText(searchView.text)

            // hide the searchView(search suggestion box)
            searchView.hide()

            // creating co-routine scope to run search method
            viewModel.search(this, searchView.text.toString())
            false
        }

        // when current location button press
        buttonCurrentLocation.setOnClickListener {
            // it will set current location in mapView
            viewModel.getCurrentLocation(this)
        }

        buttonSubmit.setOnClickListener {
            showProgress()
            viewModel.uploadDetails(
                this,
                StationBasic(
                    editTextStationName.text.toString(),
                    editTextStartingPrice.text.toString().toInt(),
                    null
                ),

                StationAdvance_DS(
                    editTextPolicies.text.toString(),
                    getAmenities(),
                    getAccessTime()
                )
            )
        }

        viewModel.timePicker.liveDataTimePicker.observe(this) {
            if (openFlag) {
                editTextOpenTime.setText(viewModel.timePicker.format12(it))
                openFlag = false
            } else {
                editTextCloseTime.setText(viewModel.timePicker.format12(it))
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
                editTextStationName.setText(it.name)
                editTextStartingPrice.setText(it.price.toString())
            }
        }
        viewModel.liveDataStationAdvance.observe(this) {
            it?.let {
                editTextPolicies.setText(it.policies)
                editTextOpenTime.setText(it.accessHours.open)
                editTextCloseTime.setText(it.accessHours.close)
                loadDaysSwitch(it.accessHours.selectedDays)
                loadAmenities(it.amenities)
            }
        }
        viewModel.liveDataStationLocation.observe(this) {
            it?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                viewModel.fillAddress(this, editTextAddress, geoPoint)
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
                Toast.makeText(
                    this, "Information Save Successfully", Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this, "Failed Save Information", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getAmenities(): List<String> {
        val selectedAmenities: ArrayList<String> = ArrayList()
        selectedAmenities.apply {
            for (id in chipGroupAmenities.checkedChipIds) {
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
                getString(R.string.ev_charging) -> (chipGroupAmenities[0] as Chip).isChecked = true
                getString(R.string.valet) -> (chipGroupAmenities[1] as Chip).isChecked = true
                getString(R.string.garage) -> (chipGroupAmenities[2] as Chip).isChecked = true
                getString(R.string.on_site_staff) -> (chipGroupAmenities[3] as Chip).isChecked = true
                getString(R.string.wheelchair_accessible) -> (chipGroupAmenities[4] as Chip).isChecked = true
            }
        }
    }

    private fun getAccessTime(): DataAccessHours {
        val editTextOpenTime: EditText = findViewById(R.id.editTextOpenTime)
        val editTextCloseTime: EditText = findViewById(R.id.editTextCloseTime)


        val selectedDays: ArrayList<String> = ArrayList()

        selectedDays.apply {
            for (id in chipGroupDays.checkedChipIds) {
                when (id) {
                    R.id.chipMonday -> getString(R.string.monday)
                    R.id.chipTuesday -> getString(R.string.tuesday)
                    R.id.chipWednesday -> getString(R.string.wednesday)
                    R.id.chipThursday -> getString(R.string.thursday)
                    R.id.chipFriday -> getString(R.string.friday)
                    R.id.chipSaturday -> getString(R.string.saturday)
                    R.id.chipSunday -> getString(R.string.sunday)
                }
            }
        }

        return DataAccessHours(
            editTextOpenTime.text.toString(),
            editTextCloseTime.text.toString(),
            selectedDays
        )
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

    private fun loadDaysSwitch(list: List<String>) {
        for (i in list) {
            when (i) {
                getString(R.string.monday) -> (chipGroupDays[0] as Chip).isChecked = true
                getString(R.string.tuesday) -> (chipGroupDays[1] as Chip).isChecked = true
                getString(R.string.wednesday) -> (chipGroupDays[2] as Chip).isChecked = true
                getString(R.string.thursday) -> (chipGroupDays[3] as Chip).isChecked = true
                getString(R.string.friday) -> (chipGroupDays[4] as Chip).isChecked = true
                getString(R.string.saturday) -> (chipGroupDays[5] as Chip).isChecked = true
                getString(R.string.sunday) -> (chipGroupDays[6] as Chip).isChecked = true
            }
        }
    }
}