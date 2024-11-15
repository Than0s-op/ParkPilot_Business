package com.application.parkpilotreg.viewModel

import android.content.Context
import android.net.Uri
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.parkpilotreg.StationBasic as DC_StationBasic
import com.application.parkpilotreg.StationLocation as DC_StationLocation
import com.application.parkpilotreg.StationAdvance as DC_StationAdvance
import com.application.parkpilotreg.User
import com.application.parkpilotreg.module.OSM
import com.application.parkpilotreg.module.PhotoPicker
import com.application.parkpilotreg.module.TimePicker
import com.application.parkpilotreg.module.firebase.Storage
import com.google.firebase.firestore.GeoPoint as FS_GeoPoint
import com.application.parkpilotreg.module.firebase.database.StationAdvance as FS_StationAdvance
import com.application.parkpilotreg.module.firebase.database.StationBasic as FS_StationBasic
import com.application.parkpilotreg.module.firebase.database.StationLocation as FS_StationLocation
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint as OSM_GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SpotRegisterViewModel : ViewModel() {
    private lateinit var mapViewOSM: OSM

    val timePicker = TimePicker("pick the time", TimePicker.CLOCK_12H)
    lateinit var photoPicker: PhotoPicker
    val imageViewsUri = arrayOf<Uri?>(null, null, null)
    val liveDataStationBasic = MutableLiveData<DC_StationBasic?>()
    val liveDataStationAdvance = MutableLiveData<DC_StationAdvance?>()
    val liveDataStationLocation = MutableLiveData<FS_GeoPoint?>()
    val isUploaded = MutableLiveData<Boolean>()
    lateinit var marker: Marker
    val liveDataImages = MutableLiveData<List<Uri>>()


    fun init(context: Context, mapView: MapView) {
        mapViewOSM = OSM(mapView)
        photoPicker = PhotoPicker(context)
        marker = mapViewOSM.addMarker(mapViewOSM.center)
        mapViewOSM.touchLocationObserver.observe(context as AppCompatActivity) {
            marker.position = it
        }
    }

    fun timePicker(manager: FragmentManager, tag: String?) {
        timePicker.showTimePicker(manager, tag)
    }

    fun loadActivity(onComplete: () -> Unit) {
        viewModelScope.launch {
            liveDataStationBasic.value = FS_StationBasic().basicGet(User.UID)
            liveDataStationAdvance.value = FS_StationAdvance().advanceGet(User.UID)
            liveDataStationLocation.value = FS_StationLocation().locationGet(User.UID)
            liveDataImages.value = Storage().parkSpotPhotoGet(User.UID)
            onComplete()
        }
    }

    fun search(context: Context, searchQuery: String) {
        // suspend function. it will block processes/UI thread ( you can run this function on another thread/coroutine)
        val address = mapViewOSM.search(context, searchQuery)
        // when search method got the search result without empty body
        address?.let {
            val geoPoint = OSM_GeoPoint(it.latitude, it.longitude)
            mapViewOSM.setCenter(geoPoint)
            marker.position = geoPoint
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            // suspend function. It will block the processes/UI thread
            val currentLocation = mapViewOSM.getLastKnowLocation(context)

            // when we got user current location
            currentLocation?.let {
                // set the user's current location as center of map
                setMarker(it)
            }
        }
    }

    fun setMarker(geoPoint: OSM_GeoPoint) {
        mapViewOSM.setCenter(geoPoint)
        marker.position = geoPoint
    }

    fun imagePicker() {
        photoPicker.showPhotoPicker()
    }

    fun fillAddress(context: Context, editText: EditText, geoPoint: org.osmdroid.util.GeoPoint) {
        val address = mapViewOSM.getAddress(context, geoPoint)
        address?.let {
            editText.setText(it.getAddressLine(0))
        }
    }

    fun uploadDetails(context: Context, basic: DC_StationBasic, advance: DC_StationAdvance) {
        var result = true
        viewModelScope.launch {
            marker.position.apply {
                result = FS_StationLocation().locationSet(
                    DC_StationLocation(null, FS_GeoPoint(latitude, longitude)),
                    User.UID
                ) and result
            }

            result = FS_StationBasic().basicSet(basic, User.UID) and result

            result = FS_StationAdvance().advanceSet(advance, User.UID) and result

            result = Storage().parkSpotPhotoPut(User.UID, imageViewsUri) and result

            isUploaded.value = result
        }
    }

}