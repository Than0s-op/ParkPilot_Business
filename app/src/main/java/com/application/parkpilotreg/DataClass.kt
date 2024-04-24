package com.application.parkpilotreg

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint as FirebaseGeoPoint
import org.osmdroid.util.GeoPoint as OSMGeoPoint


data class ParkPilotMapLegend(val title: String, val UID: String, val coordinates: OSMGeoPoint)
data class UserCollection(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val gender: String
)

data class UserProfile(val userName: String, val userPicture: Uri?=null)
data class QRCodeCollection(
    val key: String,
    val to: Int,
    val from: Timestamp = Timestamp.now(),
    val valid: Boolean = true
)

data class StationLocation(val stationUid: String?, val coordinates: FirebaseGeoPoint)
data class StationBasic(val name: String?, val price: Int?, val reserved: Int?)

data class StationAdvance(
    val policies: String,
    val amenities: List<String>,
    val accessHours: AccessHours,
)

data class Time(val hours: Int, val minute: Int)

data class AccessHours(
    val open: String,
    val close: String,
    val selectedDays: List<String>
)

data class Feedback(val UID: String = User.UID, val rating: Float, val message: String)