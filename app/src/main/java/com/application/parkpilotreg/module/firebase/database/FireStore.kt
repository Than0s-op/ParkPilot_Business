package com.application.parkpilotreg.module.firebase.database

import android.net.Uri
import androidx.core.net.toUri
import com.application.parkpilotreg.AccessHours
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.QRCodeCollection
import com.application.parkpilotreg.StationAdvance
import com.application.parkpilotreg.StationBasic
import com.application.parkpilotreg.StationLocation
import com.application.parkpilotreg.Time
import com.application.parkpilotreg.UserCollection
import com.application.parkpilotreg.UserProfile
import com.application.parkpilotreg.module.firebase.Storage
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
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

    suspend fun isUnique(userName: String): Boolean {
        val aggregateCount =
            fireStore.collection(collectionName).whereEqualTo(this.userName, userName).count()
                .get(AggregateSource.SERVER).await()
        return aggregateCount.count == 0L
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
    private val reserved = "reserved"
    suspend fun basicGet(documentID: String): StationBasic? {
        var result: StationBasic? = null
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            if (get(name) != null) {
                result = StationBasic(
                    get(name) as String,
                    (get(price) as Long).toInt(),
                    (get(reserved) as Long).toInt()
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
            price to stationBasic.price,
            reserved to stationBasic.reserved
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
    private val policies = "policies"
    private val amenities = "amenities"
    private val accessHours = "accessHours"
    private val openTime = "openTime"
    private val closeTime = "closeTime"
    private val available = "available"

    suspend fun advanceGet(documentID: String): StationAdvance? {
        var result: StationAdvance? = null
        fireStore.collection(collectionName).document(documentID).get().await().apply {
            if (get(amenities) != null) {
                result = StationAdvance(get(policies) as String,
                    get(amenities) as ArrayList<String>,
                    (get(accessHours) as Map<String, Any>).let {
                        AccessHours(
                            it[openTime] as String,
                            it[closeTime] as String,
                            it[available] as List<String>
                        )
                    })
            }
        }
        return result
    }

    suspend fun advanceSet(stationAdvance: StationAdvance, documentID: String): Boolean {
        // for success result
        var result = false

        // data mapping
        val map = mapOf(
            policies to stationAdvance.policies,
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

class FreeSpotStore : FireStore() {
    private val storage by lazy { Storage() }
    suspend fun set(spot: FreeSpot): Boolean {
        var result = true
        fireStore.collection(NAME).document(spot.id).set(
            mapOf(
                LAND_MARK to spot.landMark,
                LOCATION to spot.location,
                POLICIES to spot.policies,
            )
        ).addOnFailureListener {
            result = false
        }.await()
        result = storage.setFreeSpotImages(
            uid = spot.id,
            uriList = spot.images
        ) and result
        return result
    }

    suspend fun get(documentId: String): FreeSpot {
        val imageList = storage.getFreeSpotImages(documentId)
        val document = fireStore.collection(NAME)
            .document(documentId)
            .get()
            .await()
            .data
        return FreeSpot(
            id = documentId,
            landMark = document!![LAND_MARK].toString(),
            location = document[LOCATION] as GeoPoint,
            policies = document[POLICIES].toString(),
            images = imageList
        )
    }

    suspend fun remove(documentId: String): Boolean {
        var result = true
        result = result and storage.removeFreeSpotImages(documentId)

        fireStore.collection(NAME).document(documentId).delete().addOnFailureListener {
            result = false
        }.await()
        return result
    }

    suspend fun getAllSpots(): List<FreeSpot> {
        val spotList = ArrayList<FreeSpot>()
        val documents = fireStore.collection(NAME).get().await()
        for (document in documents) {
            spotList.add(
                FreeSpot(
                    id = document.id,
                    landMark = document.data[LAND_MARK].toString(),
                    location = document.data[LOCATION] as GeoPoint,
                    policies = document.data[POLICIES].toString(),
                    images = storage.getFreeSpotImages(document.id)
                )
            )
        }
        return spotList
    }


    companion object {
        private const val NAME = "free_spots"
        private const val LAND_MARK = "land_mark"
        private const val LOCATION = "location"
        private const val POLICIES = "policies"
    }
}