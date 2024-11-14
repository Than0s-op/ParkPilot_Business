package com.application.parkpilotreg.module.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.imageLoader
import coil.request.ImageRequest
import com.application.parkpilotreg.Utils
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class Storage {
    private val storageRef = Firebase.storage.reference
    suspend fun userProfilePhotoPut(uid: String, uri: Uri?): Uri? {
        val childRef = storageRef.child("user_profile_photo/${uid}")
//        val request = ImageRequest.Builder(context)
//            .data(photo)
//            .size(600, 600)
//            .build()
//        val drawable = context.imageLoader.execute(request).drawable
//
//        // this next leve logic present on firebase doc
//        val bitmap = (drawable as BitmapDrawable).bitmap
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
        if (uri == null) return null
        if (Utils.isLocalUri(uri)) {
            childRef.putFile(uri)
        }
        return userProfilePhotoGet(uid)
    }

    suspend fun userProfilePhotoGet(uid: String): Uri? {
        return try {
            storageRef.child("user_profile_photo/${uid}").downloadUrl.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun parkSpotPhotoPut(uid: String, photosUri: Array<Uri?>): Boolean {
        val path = "parkSpot/${uid}/"
        var result = true

        for ((cnt, uri) in photosUri.withIndex()) {
            val childRef = storageRef.child("$path${cnt}")
            if (uri == null) {
                childRef.delete()
                continue
            }

//            val request = ImageRequest.Builder(context)
//                .data(uri)
//                .size(600, 600)
//                .build()
//            val drawable = context.imageLoader.execute(request).drawable
//
//            // this next leve logic present on firebase doc
//            val bitmap = (drawable as BitmapDrawable).bitmap
//            val baos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            val data = baos.toByteArray()
            if (Utils.isLocalUri(uri)) {
                childRef.putFile(uri).addOnFailureListener {
                    result = false
                }.await()
            }
        }
        return result
    }

    suspend fun parkSpotPhotoGet(uid: String): List<Uri> {
        val list = storageRef.child("parkSpot/${uid}/").listAll().await()
        val imagesUri = ArrayList<Uri>()
        for (item in list.items) {
            imagesUri.add(item.downloadUrl.await())
        }
        return imagesUri
    }

    suspend fun setFreeSpotImages(uid: String, uriList: List<Uri?>): Boolean {
        var result = true
        val path = "free_spot/${uid}/"

        for ((cnt, uri) in uriList.withIndex()) {
            val childRef = storageRef.child("$path${cnt}")
            if (uri == null) {
                childRef.delete()
                continue
            }
            if(Utils.isLocalUri(uri)) {
                childRef.putFile(uri).addOnFailureListener {
                    result = false
                }.await()
            }
        }
        return result
    }

    suspend fun getFreeSpotImages(uid: String): List<Uri> {
        val list = storageRef.child("free_spot/${uid}/").listAll().await()
        val imagesUri = ArrayList<Uri>()
        for (item in list.items) {
            imagesUri.add(item.downloadUrl.await())
        }
        return imagesUri
    }

    suspend fun removeFreeSpotImages(uid: String): Boolean {
        var result = true
        val list = storageRef.child("free_spot/${uid}/").listAll().await()
        for (item in list.items) {
            item.delete().addOnFailureListener {
                result = false
            }
        }
        return result
    }
}