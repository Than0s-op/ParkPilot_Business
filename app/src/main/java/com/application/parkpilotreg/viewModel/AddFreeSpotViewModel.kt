package com.application.parkpilotreg.viewModel

import android.content.Context
import android.net.Uri
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.module.OSM
import com.application.parkpilotreg.module.PhotoPicker
import com.application.parkpilotreg.module.firebase.database.FreeSpotStore
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AddFreeSpotViewModel : ViewModel() {
    private lateinit var mapViewOSM: OSM
    lateinit var marker: Marker
    private val freeSpotStore by lazy { FreeSpotStore() }
    val imageViewsUri = mutableListOf<Uri?>(null, null, null)
    val freeSpot = MutableLiveData<FreeSpot>()

    fun init(context: Context, mapView: MapView) {
        mapViewOSM = OSM(mapView)
        marker = mapViewOSM.addMarker(mapViewOSM.center)
        mapViewOSM.touchLocationObserver.observe(context as AppCompatActivity) {
            marker.position = it
        }
    }
    fun getFreeSpot(documentId:String){
        viewModelScope.launch {
            freeSpot.value = freeSpotStore.get(documentId)
        }
    }

    fun set(
        spot: FreeSpot,
        onSuccess: () -> Unit = {},
        onFailed: () -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            when (freeSpotStore.set(spot)) {
                true -> onSuccess()
                false -> onFailed()
            }
            onComplete()
        }
    }

    fun search(context: Context, searchQuery: String) {
        // suspend function. it will block processes/UI thread ( you can run this function on another thread/coroutine)
        val address = mapViewOSM.search(context, searchQuery)
        // when search method got the search result without empty body
        address?.let {
            val geoPoint = GeoPoint(it.latitude, it.longitude)
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

    fun setMarker(geoPoint: GeoPoint) {
        mapViewOSM.setCenter(geoPoint)
        marker.position = geoPoint
    }

    fun fillAddress(context: Context, editText: EditText, geoPoint: GeoPoint) {
        val address = mapViewOSM.getAddress(context, geoPoint)
        address?.let {
            editText.setText(it.getAddressLine(0))
        }
    }
}