package com.application.parkpilotreg.module

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionRequest {

    fun hasLocationPermission(context: Context): Boolean {
        val read = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return read == PackageManager.PERMISSION_GRANTED
    }

    fun locationPermissionRequest(context:Context): Boolean {
        if (hasLocationPermission(context)) return true
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            123
        )
        return hasLocationPermission(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun gpsPermissionRequest(context:Context) {
        if (locationPermissionRequest(context)) {
            val interval: Long = 1000 * 60 * 1
            val fastestInterval: Long = 1000 * 50

            try {
                val googleApiClient =
                    GoogleApiClient.Builder(context).addApi(LocationServices.API).build()
                googleApiClient.connect()
                val locationRequest: LocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(interval)
                    .setFastestInterval(fastestInterval)
                val locationSettingsRequestBuilder =
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                locationSettingsRequestBuilder.setAlwaysShow(false)

                // coroutine scope
                GlobalScope.launch {
                    val locationSettingsResult: LocationSettingsResult =
                        LocationServices.SettingsApi.checkLocationSettings(
                            googleApiClient,
                            locationSettingsRequestBuilder.build()
                        ).await()
                    if (locationSettingsResult.status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        locationSettingsResult.status.startResolutionForResult(
                            context as Activity,
                            0
                        )
                    }
                }

            } catch (e: Exception) {
                Log.d("Permission Request", "${e.stackTrace}")
            }
        }
    }

}