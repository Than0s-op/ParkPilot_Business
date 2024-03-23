package com.application.parkpilotreg.module

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.size.Scale
import coil.transform.CircleCropTransformation

class PhotoLoader {
    suspend fun getImage(
        context: Context, imageUrl: Any, width: Int = 192, height: Int = 192
    ): ImageResult {
        // request for profile image of user
        val profileImageRequest = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(width, height)
            .scale(Scale.FIT)
            .transformations(CircleCropTransformation())
            .build()
        return context.imageLoader.execute(profileImageRequest)
    }
}