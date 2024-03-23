package com.application.parkpilotreg.module

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.application.parkpilotreg.ParkPilotMapLegend
import com.application.parkpilotreg.R
import com.google.android.gms.location.FusedLocationProviderClient
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


class OSM<Act : AppCompatActivity>(private val mapView: MapView, private val activity: Act) {

    // object creation of geocoder
    private val geocoder: Geocoder = Geocoder(activity)

    // object creation of fusedLocation
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

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

    fun search(searchQuery: String): Address? {
        // this is deprecated in API 33
        try {
            // It can throw exception
            val result = geocoder.getFromLocationName(searchQuery, 1)

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

    fun getAddress(coordinates:GeoPoint):Address?{
        // this is deprecated in API 33
        try {
            // It can throw exception
            val result = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude,1)

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

    suspend fun getLastKnowLocation(): GeoPoint? {

        // permission class object creation
        val activity = PermissionRequest(activity)

        // current location set to null (if permission is not granted, we can't enter in "if" block)
        var currentLocation: GeoPoint? = null

        // check is location permission granted or not. If Not request to user for location permission
        if (activity.locationPermissionRequest()) {

            // request to turn on location(GPS)
            activity.GPSPermissionRequest()

            // below code will give last know location of user. But It will "null" if user's GPS is turned off
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        currentLocation = GeoPoint(location.latitude, location.longitude)
                    }
                }.await()
        }
        return currentLocation
    }


    //    Fast Overlay (add this)
//    The fast overlay is great if you have a huge number points to render and they all share the same icon. It is optimized to render over 100k points, however performance will vary based on hardware.
    fun setPinsOnPosition(
        geoPoints: ArrayList<ParkPilotMapLegend>,
        singleTapTask: (String) -> Unit
    ) {

        // contain all pins with "single tap" and "long press" binding
        val overlayItemArrayList = ArrayList<OverlayItem>()

        // create pin image
        val markerDrawable = ContextCompat.getDrawable(activity, R.drawable.map_marker)

        for (geoPoint in geoPoints) {
            val overlayItem = OverlayItem(geoPoint.title, geoPoint.UID, geoPoint.coordinates)
            overlayItem.setMarker(
                writeOnDrawable(
                    R.drawable.map_marker,
                    geoPoint.title
                )
            )
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
            activity
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

    private fun writeOnDrawable(drawableId: Int, text: String): BitmapDrawable {
        val bm =
            Bitmap.createBitmap(AppCompatResources.getDrawable(activity, drawableId)!!.toBitmap())
        val paint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.BLACK
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        val canvas = Canvas()
        canvas.drawText(text, (bm.height / 2).toFloat(), (bm.height / 2).toFloat(), paint)
        return BitmapDrawable(activity.resources, bm)
    }
}