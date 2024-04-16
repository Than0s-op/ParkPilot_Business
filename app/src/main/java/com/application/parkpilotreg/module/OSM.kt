package com.application.parkpilotreg.module

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.application.parkpilotreg.ParkPilotMapLegend
import com.application.parkpilotreg.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.ItemizedOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


class OSM(private val mapView: MapView) {

    // it will store latitude and longitude when tap on screen
    val touchLocationObserver: MutableLiveData<GeoPoint> = MutableLiveData<GeoPoint>()
    val center = GeoPoint(18.50099198033669, 73.85907568230525)

    init {
        // I don't know
        Configuration.getInstance().userAgentValue = "ParkPilot"

        // mapView default settings
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(17.0)
        mapView.setMultiTouchControls(true)
        mapView.isClickable = true
        mapView.setBuiltInZoomControls(false)

        // temp
        setCenter(center)

        val mRotationGestureOverlay = RotationGestureOverlay(mapView)
        mRotationGestureOverlay.isEnabled = true
        mapView.overlays.add(mRotationGestureOverlay)

        mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                onTouch(event)
            }
            false
        }
    }

    private fun onTouch(event: MotionEvent) {
        val projection = mapView.projection
        val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt())

        mapView.invalidate() // Redraw the map

        // Save the coordinates (latitude and longitude) for later use
        touchLocationObserver.value = GeoPoint(geoPoint.latitude, geoPoint.longitude)
    }


    fun setCenter(coordinates: GeoPoint) {
        mapView.controller.setCenter(GeoPoint(coordinates.latitude, coordinates.longitude))
    }

    fun search(context: Context, searchQuery: String): Address? {
        // this is deprecated in API 33
        try {
            // It can throw exception
            val result = Geocoder(context).getFromLocationName(searchQuery, 1)

            // If result has result then...
            if (result != null && result.size != 0) {
                return result.first()
            }
        } catch (e: Exception) {
            println("Exception:OSM:search ${e.message}")
        }
        //
        return null
    }

    fun getAddress(context: Context, coordinates: GeoPoint): Address? {
        // this is deprecated in API 33
        try {
            // It can throw exception
            val result =
                Geocoder(context).getFromLocation(coordinates.latitude, coordinates.longitude, 1)

            // If result has result then...
            if (result != null && result.size != 0) {
                return result.first()
            }
        } catch (e: Exception) {
            println("Exception:OSM:search ${e.message}")
        }

        return null
    }

    suspend fun getLastKnowLocation(context: Context): GeoPoint? {

        // permission class object creation
        val activity = PermissionRequest()

        // current location set to null (if permission is not granted, we can't enter in "if" block)
        var currentLocation: GeoPoint? = null

        // check is location permission granted or not. If Not request to user for location permission
        if (activity.locationPermissionRequest(context)) {

            // request to turn on location(GPS)
            activity.gpsPermissionRequest(context)

            try {
                // below code will give last know location of user. But It will "null" if user's GPS is turned off
                LocationServices.getFusedLocationProviderClient(context).lastLocation.await().let {
                    currentLocation = GeoPoint(it.latitude, it.longitude)
                }
            } catch (_: Exception) {
            }

        }
        return currentLocation
    }


    //    Fast Overlay (add this)
//    The fast overlay is great if you have a huge number points to render and they all share the same icon. It is optimized to render over 100k points, however performance will vary based on hardware.
    fun setPinsOnPosition(
        context: Context,
        geoPoints: ArrayList<ParkPilotMapLegend>,
        singleTapTask: (String) -> Unit
    ) {

        // contain all pins with "single tap" and "long press" binding
        val overlayItemArrayList = ArrayList<OverlayItem>()

        // create pin image
        val markerDrawable = ContextCompat.getDrawable(context, R.drawable.map_marker)

        for (geoPoint in geoPoints) {
            val overlayItem = OverlayItem(geoPoint.title, geoPoint.UID, geoPoint.coordinates)
            overlayItem.setMarker(markerDrawable)
            overlayItemArrayList.add(overlayItem)
        }

        // binding "single tap" and "long press" event to all overlay Items
        val locationOverlay: ItemizedOverlay<OverlayItem> = ItemizedIconOverlay(
            overlayItemArrayList,
            object : OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(i: Int, overlayItem: OverlayItem): Boolean {
                    singleTapTask(overlayItem.snippet)
                    return true // Handled this event.
                }

                override fun onItemLongPress(i: Int, overlayItem: OverlayItem): Boolean {
                    return false
                }
            },
            context
        )

        // update overlays(pins) on mapView
        mapView.overlays.add(locationOverlay)
    }

    fun addMarker(coordinates: GeoPoint): Marker {
        val marker = Marker(mapView)
        marker.position = coordinates
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        return marker
    }

    fun removeMarker(marker: Marker) {
        mapView.overlays.remove(marker)
    }
}