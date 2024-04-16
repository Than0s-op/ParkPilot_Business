package com.application.parkpilotreg.module.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class Storage {
    private val storageRef = Firebase.storage.reference
    suspend fun userProfilePhotoPut(context: Context, uid: String, photo: Any): Uri? {
        val childRef = storageRef.child("user_profile_photo/${uid}")
        val request = ImageRequest.Builder(context)
            .data(photo)
            .size(600, 600)
            .build()
        val drawable = context.imageLoader.execute(request).drawable

        // this next leve logic present on firebase doc
        val bitmap = (drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        childRef.putBytes(data).await()
        return userProfilePhotoGet(uid)
    }

    suspend fun userProfilePhotoGet(uid: String): Uri? {
        return try {
            storageRef.child("user_profile_photo/${uid}").downloadUrl.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun parkSpotPhotoPut(context: Context, uid: String, photosUri: Array<Uri?>): Boolean {
        val path = "parkSpot/${uid}/"
        var result = true

        for ((cnt, uri) in photosUri.withIndex()) {
            val childRef = storageRef.child("$path${cnt}")
            if (uri == null) {
                childRef.delete()
                continue
            }

            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(600, 600)
                .build()
            val drawable = context.imageLoader.execute(request).drawable

            // this next leve logic present on firebase doc
            val bitmap = (drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            childRef.putBytes(data).addOnFailureListener {
                result = false
            }.await()
        }
        return result
    }

    suspend fun parkSpotPhotoGet(uid: String): List<Uri> {
        val list = storageRef.child("parkSpot/${uid}/").listAll().await()
        val imagesUri = ArrayList<Uri>()
        for (i in list.items) {
            imagesUri.add(i.downloadUrl.await())
        }
        return imagesUri
    }
}