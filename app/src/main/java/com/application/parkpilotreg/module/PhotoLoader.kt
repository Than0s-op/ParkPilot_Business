package com.application.parkpilotreg.module

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.size.Scale
import coil.transform.CircleCropTransformation

class PhotoLoader {
    suspend fun getImage(context: Context, imageUrl: Any, isCircleCrop:Boolean=true): ImageResult {
        // request for profile image of user
        val profileImageRequest = ImageRequest.Builder(context)
            .data(imageUrl)
            .scale(Scale.FIT)

        if(isCircleCrop)
            profileImageRequest.transformations(CircleCropTransformation())

        return context.imageLoader.execute(profileImageRequest.build())
    }
}