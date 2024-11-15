package com.application.parkpilotreg.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.request.ImageResult
import com.application.parkpilotreg.User
import com.application.parkpilotreg.UserCollection
import com.application.parkpilotreg.UserProfile
import com.application.parkpilotreg.module.DatePicker
import com.application.parkpilotreg.module.PhotoPicker
import com.application.parkpilotreg.module.firebase.Storage
import com.application.parkpilotreg.module.firebase.database.UserAdvance
import com.application.parkpilotreg.module.firebase.database.UserBasic
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class UserRegisterViewModel(context: Context) : ViewModel() {
    // it will store user profile image's Uri
    var photoUrl: Uri? = null

    // live data, it will help to "View" to get data
    val userInformation = MutableLiveData<UserCollection?>()
    val userProfile = MutableLiveData<UserProfile?>()
    val imageLoaderResult = MutableLiveData<ImageResult>()
    val isUploaded = MutableLiveData<Boolean>()
    val isUnique = MutableLiveData<Boolean>()

    private val userBasic = UserBasic()
    private val userAdvance = UserAdvance()
    private val storage = Storage()
    private val calendar = Calendar.getInstance()
    private val endDate = calendar.let {
        it.add(Calendar.YEAR, -18)
        it.timeInMillis
    }
    private val startDate = calendar.let {
        it.add(Calendar.YEAR, -50)
        it.timeInMillis
    }
    val datePicker = DatePicker(startDate, endDate)
    val photoPicker = PhotoPicker(context)


//    // it will get user detail from user collection
//    fun getUserDetails() {
//        viewModelScope.launch {
//            // call to fireStore
//
//        }
//    }

    fun isUnique(userName: String) {
        viewModelScope.launch {
            isUnique.value = userBasic.isUnique(userName)
        }
    }

    fun getProfileDetails(onComplete: () -> Unit) {
        viewModelScope.launch {
            userInformation.value = userAdvance.userGet(User.UID)
            val result = userBasic.getProfile(User.UID)
            if (result != null) {
                userProfile.value =
                    UserProfile(result.userName, storage.userProfilePhotoGet(User.UID))
            } else {
                if (Firebase.auth.currentUser?.displayName != null) {
                    userProfile.value = UserProfile(
                        Firebase.auth.currentUser!!.displayName!!,
                        Firebase.auth.currentUser!!.photoUrl
                    )
                } else userProfile.value = null
            }
            onComplete()
        }
    }

    //
    fun saveUserData(context: Context, userCollection: UserCollection, userProfile: UserProfile) {
        var result = true
        viewModelScope.launch {
            result = result and userAdvance.userSet(userCollection, User.UID)

            storage.userProfilePhotoPut(
                User.UID,
                photoUrl
            )

            result =
                result and userBasic.setProfile(UserProfile(userProfile.userName), User.UID)

            isUploaded.value = result
        }
    }

    // format should be in day,month,year
    fun getAge(birthDate: String): String {
        // getting today's date
        val current = LocalDate.now()

        // parsing the "birthDate" string to get birth (day, month, year)
        val birthYear = birthDate.substring(6).toInt()
        val birthMonth = birthDate.substring(3, 5).toInt()
        val birthDay = birthDate.substring(0, 2).toInt()

        // finding the age of the user ( "-1"  to handel current year)
        var age = current.year - birthYear - 1

        // to check user birthDay has gone or not in current year. if yes increment age by 1
        if (birthMonth < current.monthValue || (birthMonth == current.monthValue && birthDay <= current.dayOfMonth)) age++

        // return the age
        return age.toString()
    }

    private fun getAvatar(context: Context, userName: String): Bitmap {
        return AvatarGenerator.AvatarBuilder(context)
            .setLabel(userName)
            .setAvatarSize(240)
            .setTextSize(30)
            .build().bitmap
    }
}