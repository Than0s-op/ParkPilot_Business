package com.application.parkpilotreg.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.application.parkpilotreg.R
import com.application.parkpilotreg.StationBasic
import com.application.parkpilotreg.viewModel.ParkRegisterViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.application.parkpilotreg.AccessHours as DataAccessHours
import com.application.parkpilotreg.StationAdvance as StationAdvance_DS

class ParkRegisterActivity : AppCompatActivity(R.layout.park_register) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editTextOpenTime: EditText = findViewById(R.id.editTextOpenTime)
        val editTextCloseTime: EditText = findViewById(R.id.editTextCloseTime)
        val editTextStationName: EditText = findViewById(R.id.editTextStationName)
        val buttonLocationPick: Button = findViewById(R.id.buttonLocationPick)
        val editTextLocation: EditText = findViewById(R.id.editTextLocation)
        val buttonSubmit: Button = findViewById(R.id.buttonSubmit)

        val editTextGettingThere: EditText = findViewById(R.id.editTextGettingThere)
        val editTextStartingPrice: EditText = findViewById(R.id.editTextStartingPrice)

        val imageViews = arrayOf(
            findViewById<ImageView>(R.id.imageView1),
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


        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ParkRegisterViewModel(mapView, this@ParkRegisterActivity) as T
            }
        })[ParkRegisterViewModel::class.java]


        viewModel.loadActivity()

        buttonLocationPick.setOnClickListener {
            dialogBox.show()
        }

        dialogBox.setOnDismissListener {
            viewModel.fillAddress(editTextLocation,viewModel.marker.position)
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
                imageViews[0].load(R.drawable.add_circle_icon)
                viewModel.imageViewsUri[0] = null
            } else {
                flagImageView1 = true
                viewModel.imagePicker()
            }
        }

        var flagImageView2 = false
        imageViews[1].setOnClickListener {
            if (viewModel.imageViewsUri[1] != null) {
                imageViews[1].load(R.drawable.add_circle_icon)
                viewModel.imageViewsUri[1] = null
            } else {
                flagImageView2 = true
                viewModel.imagePicker()
            }
        }

        imageViews[2].setOnClickListener {
            if (viewModel.imageViewsUri[2] != null) {
                imageViews[2].load(R.drawable.add_circle_icon)
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
            viewModel.search(searchView.text.toString())
            false
        }

        // when current location button press
        buttonCurrentLocation.setOnClickListener {
            // it will set current location in mapView
            viewModel.getCurrentLocation()
        }

        buttonSubmit.setOnClickListener {
            viewModel.uploadDetails(
                this,
                StationBasic(
                    editTextStationName.text.toString(),
                    editTextStartingPrice.text.toString().toInt(),
                    null
                ),

                StationAdvance_DS(
                    getThinkShouldYouKnow(),
                    getAmenities(),
                    editTextGettingThere.text.toString(),
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
                editTextGettingThere.setText(it.gettingThere)
                val editTextThinkShouldYouKnow: EditText =
                    findViewById(R.id.editTextThinkShouldYouKnow)
                editTextThinkShouldYouKnow.setText(loadThinkShouldYouKnow(it.thinkShouldYouKnow))
                editTextOpenTime.setText(it.accessHours.open)
                editTextCloseTime.setText(it.accessHours.close)
                loadDaysSwitch(it.accessHours.selectedDays)
            }
        }
        viewModel.liveDataStationLocation.observe(this){
            it?.let{
                viewModel.fillAddress(editTextLocation,GeoPoint(it.latitude,it.longitude))
            }
        }
        viewModel.liveDataImages.observe(this) {
            for (i in it.indices) {
                imageViews[i].load(it[i])
                viewModel.imageViewsUri[i] = it[i]
            }
        }
        viewModel.isUploaded.observe(this) { isUploaded ->
            if (isUploaded) {
                Toast.makeText(
                    this, "Information Save Successfully", Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                Toast.makeText(
                    this, "Failed Save Information", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getAmenities(): List<String> {
        val switchValet: MaterialSwitch = findViewById(R.id.switchValet)
        val switchEVCharging: MaterialSwitch = findViewById(R.id.switchEVCharging)
        val selectedAmenities: ArrayList<String> = ArrayList()

        if (switchValet.isChecked) selectedAmenities.add("valet")
        if (switchEVCharging.isChecked) selectedAmenities.add("ev_charging")

        return selectedAmenities
    }

    private fun loadAmenities(list: List<String>) {
        val switchValet: MaterialSwitch = findViewById(R.id.switchValet)
        val switchEVCharging: MaterialSwitch = findViewById(R.id.switchEVCharging)
        for (i in list) {
            when (i) {
                "valet" -> switchValet.isChecked = true
                "ev_charging" -> switchEVCharging.isChecked = true
            }
        }
    }

    private fun getAccessTime(): DataAccessHours {
        val editTextOpenTime: EditText = findViewById(R.id.editTextOpenTime)
        val editTextCloseTime: EditText = findViewById(R.id.editTextCloseTime)
        val switchMonday: MaterialSwitch = findViewById(R.id.switchMonday)
        val switchTuesday: MaterialSwitch = findViewById(R.id.switchTuesday)
        val switchWednesday: MaterialSwitch = findViewById(R.id.switchWednesday)
        val switchThursday: MaterialSwitch = findViewById(R.id.switchThursday)
        val switchFriday: MaterialSwitch = findViewById(R.id.switchFriday)
        val switchSaturday: MaterialSwitch = findViewById(R.id.switchSaturday)
        val switchSunday: MaterialSwitch = findViewById(R.id.switchSunday)

        val selectedDays: ArrayList<String> = ArrayList()

        selectedDays.apply {
            if (switchMonday.isChecked) add("monday")
            if (switchTuesday.isChecked) add("tuesday")
            if (switchWednesday.isChecked) add("wednesday")
            if (switchThursday.isChecked) add("thursday")
            if (switchFriday.isChecked) add("friday")
            if (switchSaturday.isChecked) add("saturday")
            if (switchSunday.isChecked) add("sunday")
        }

        return DataAccessHours(
            editTextOpenTime.text.toString(),
            editTextCloseTime.text.toString(),
            selectedDays
        )
    }

    private fun getThinkShouldYouKnow(): List<String> {
        val editTextThinkShouldYouKnow: EditText = findViewById(R.id.editTextThinkShouldYouKnow)
        return editTextThinkShouldYouKnow.text.split("\n")
    }

    private fun loadThinkShouldYouKnow(list: List<String>): String {
        var result = ""
        for (i in list) {
            result += "$i\n"
        }
        return result
    }

    private fun loadDaysSwitch(list: List<String>) {
        val switchMonday: MaterialSwitch = findViewById(R.id.switchMonday)
        val switchTuesday: MaterialSwitch = findViewById(R.id.switchTuesday)
        val switchWednesday: MaterialSwitch = findViewById(R.id.switchWednesday)
        val switchThursday: MaterialSwitch = findViewById(R.id.switchThursday)
        val switchFriday: MaterialSwitch = findViewById(R.id.switchFriday)
        val switchSaturday: MaterialSwitch = findViewById(R.id.switchSaturday)
        val switchSunday: MaterialSwitch = findViewById(R.id.switchSunday)
        for (i in list) {
            when (i) {
                "monday" -> switchMonday.isChecked = true
                "tuesday" -> switchTuesday.isChecked = true
                "wednesday" -> switchWednesday.isChecked = true
                "thursday" -> switchThursday.isChecked = true
                "friday" -> switchFriday.isChecked = true
                "saturday" -> switchSaturday.isChecked = true
                "sunday" -> switchSunday.isChecked = true
            }
        }
    }
}