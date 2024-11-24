package com.application.parkpilotreg.module

import android.content.Context
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData

class PhotoPicker(context: Context) {
    // Registers a photo picker activity launcher in single-select mode.
    var pickedImage = MutableLiveData<Uri?>()
    private val pickMedia =
        (context as AppCompatActivity).registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                pickedImage.value = uri
            }
        }

    fun showPhotoPicker() {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}