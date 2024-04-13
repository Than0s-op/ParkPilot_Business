package com.application.parkpilotreg.module.firebase.database

import android.net.Uri
import androidx.core.net.toUri
import com.application.parkpilotreg.AccessHours
import com.application.parkpilotreg.QRCodeCollection
import com.application.parkpilotreg.StationAdvance
import com.application.parkpilotreg.StationBasic
import com.application.parkpilotreg.StationLocation
import com.application.parkpilotreg.Time
import com.application.parkpilotreg.UserCollection
import com.application.parkpilotreg.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.application.parkpilotreg.Feedback as FeedbackData
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

open class FireStore {
    // fireStore initialization
    protected val fireStore = Firebase.firestore
}

class UserBasic : FireStore() {
    private val collectionName = "usersBasic"
    private val userName = "userName"
    private val userPicture = "userPicture"

    // it will update user name and profile image
    suspend fun setProfile(data: UserProfile, documentID: String): Boolean {
        // to store update result
        var result = false

        // data mapping
        val map = mapOf(
            userName to data.userName.trim()
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return update status
        return result
    }

    suspend fun getProfile(documentID: String): UserProfile? {
        var result: UserProfile? = null
        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            // is firstName present? if yes, it means data are present otherwise not
            if (get(userName) != null) {
                result = UserProfile(
                    get(userName) as String
                )
            }
        }
        return result
    }
}

class UserAdvance : FireStore() {
    private val collectionName = "usersAdvance"
    private val firstName = "firstName"
    private val lastName = "lastName"
    private val birthDate = "birthDate"
    private val gender = "gender"


    // To put date into user collection in specific document
    suspend fun userSet(data: UserCollection, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            firstName to data.firstName.trim(),
            lastName to data.lastName.trim(),
            birthDate to data.birthDate.trim(),
            gender to data.gender
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }

    // To get data from user collection with specific document
    suspend fun userGet(documentID: String): UserCollection? {
        var result: UserCollection? = null
        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            // is firstName present? if yes, it means data are present otherwise not
            if (get(firstName) != null) {
                result = UserCollection(
                    get(firstName) as String,
                    get(lastName) as String,
                    get(birthDate) as String,
                    get(gender) as String
                )
            }
        }
        return result
    }
}

class QRCode : FireStore() {
    private val collectionName = "qrCodes"
    private val from = "from"
    private val to = "to"
    private val validStatus = "valid"

    // To put date into user collection in specific document
    suspend fun QRCodeSet(data: QRCodeCollection, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            data.key to mapOf(
                from to data.from,
                to to data.to,
                validStatus to data.valid,
            )
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }

    // To get data from user collection with specific document
    @Suppress("UNCHECKED_CAST")
    suspend fun QRCodeGet(documentID: String): ArrayList<QRCodeCollection> {
        val result: ArrayList<QRCodeCollection> = ArrayList()
        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).get().await().let { document ->
            document.data?.let {
                val fields = it as? Map<String, Map<String, Any>>
                if (fields != null) {
                    for (field in fields) {
                        result.add(
                            QRCodeCollection(
                                key = field.key,
                                from = field.value[from] as Timestamp,
                                to = (field.value[to] as Long).toInt(),
                                valid = field.value[validStatus] as Boolean,
                            )
                        )
                    }
                }
            }
        }
        return result
    }
}

class StationLocation : FireStore() {
    private val collectionName = "stationsLocation"
    private val coordinates = "coordinates"
    suspend fun locationGet(): ArrayList<StationLocation> {
        // creating arraylist of station data class
        val result = ArrayList<StationLocation>()

        fireStore.collection(collectionName).get().await().let { collection ->
            for (document in collection) {
                result.add(StationLocation(document.id, document.data[coordinates] as GeoPoint))
            }
        }
        return result
    }

    suspend fun locationGet(documentID: String): GeoPoint? {
        var result: GeoPoint? = null
        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            // is coordinates present? if yes, it means data is present otherwise not
            get(coordinates)?.let {
                result = it as GeoPoint
            }
        }
        return result
    }

    suspend fun locationSet(stationLocation: StationLocation, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            coordinates to stationLocation.coordinates
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }
}

class StationBasic : FireStore() {
    private val collectionName = "stationBasic"
    private val name = "name"
    private val price = "price"
    private val rating = "rating"
    suspend fun basicGet(documentID: String): StationBasic? {
        var result: StationBasic? = null
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            if (get(name) != null) {
                result =
                    StationBasic(
                        get(name) as String,
                        (get(price) as Long).toInt(),
                        (get(rating) as? Double)?.toFloat()
                    )
            }
        }
        return result
    }

    suspend fun basicSet(stationBasic: StationBasic, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            name to stationBasic.name,
            price to stationBasic.price
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }
}

@Suppress("UNCHECKED_CAST")
class StationAdvance : FireStore() {
    private val collectionName = "stationAdvance"
    private val thinkShouldYouKnow = "ThinkShouldYouKnow"
    private val amenities = "amenities"
    private val accessHours = "accessHours"
    private val gettingThere = "gettingThere"
    private val openTime = "openTime"
    private val closeTime = "closeTime"
    private val available = "available"

    suspend fun advanceGet(documentID: String): StationAdvance? {
        var result: StationAdvance? = null
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            if(get(amenities) != null) {
                result = StationAdvance(
                    get(thinkShouldYouKnow) as ArrayList<String>,
                    get(amenities) as ArrayList<String>,
                    (get(accessHours) as Map<String, Any>).let {
                        AccessHours(
                            it[openTime] as String,
                            it[closeTime] as String,
                            it[available] as List<String>
                        )
                    }
                )
            }
        }
        return result
    }

    suspend fun advanceSet(stationAdvance: StationAdvance, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            thinkShouldYouKnow to stationAdvance.policies,
            amenities to stationAdvance.amenities,
            accessHours to mapOf(
                openTime to stationAdvance.accessHours.open,
                closeTime to stationAdvance.accessHours.close,
                available to stationAdvance.accessHours.selectedDays
            )
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }
}

class Feedback : FireStore() {
    private val collectionName = "feedback"
    private val rating = "rating"
    private val message = "message"

    @Suppress("UNCHECKED_CAST")
    suspend fun feedGet(documentID: String): ArrayList<FeedbackData> {
        val result = ArrayList<FeedbackData>()
        fireStore.collection(collectionName).document(documentID).get().await().let {
            val feedbacks = it.data as? Map<String, Any>

            if (feedbacks != null) {
                for (feedback in feedbacks) {
                    feedback.apply {
                        val values = value as Map<String, Any>
                        result.add(
                            FeedbackData(
                                key,
                                (values[rating] as Double).toFloat(),
                                values[message] as String
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    suspend fun feedSet(feedback: FeedbackData, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            feedback.UID to mapOf(
                rating to feedback.rating,
                message to feedback.message
            )
        )

        // await function this will block thread
        fireStore.collection(collectionName).document(documentID).set(map, SetOptions.merge())
            .addOnSuccessListener {
                // call successfully perform
                result = true
            }.await()

        // return result
        return result
    }
}