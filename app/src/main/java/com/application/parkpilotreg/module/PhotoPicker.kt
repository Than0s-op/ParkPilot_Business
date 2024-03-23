package com.application.parkpilotreg.module

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData

class PhotoPicker(context: AppCompatActivity) {
    // Registers a photo picker activity launcher in single-select mode.
    var pickedImage = MutableLiveData<Uri?>()
    private val pickMedia =
        context.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                pickedImage.value = uri
            }
        }

    fun showPhotoPicker() {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}