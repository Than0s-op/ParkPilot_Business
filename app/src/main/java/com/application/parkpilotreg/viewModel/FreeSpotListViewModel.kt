package com.application.parkpilotreg.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.module.firebase.database.FreeSpotStore
import kotlinx.coroutines.launch

class FreeSpotListViewModel : ViewModel() {
    private val freeSpotStore = FreeSpotStore()
    val freeSpotList = MutableLiveData<List<FreeSpot>>()

    fun getFreeSpotList(
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            freeSpotList.value = freeSpotStore.getAllSpots()
            onComplete()
        }
    }

    fun removeSpot(
        documentId: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            when (freeSpotStore.remove(documentId)) {
                true -> onSuccess()
                false -> onFailure()
            }
            onComplete()
        }
    }
}