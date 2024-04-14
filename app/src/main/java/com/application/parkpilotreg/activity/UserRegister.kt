package com.application.parkpilotreg.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.application.parkpilotreg.R
import com.application.parkpilotreg.UserCollection
import com.application.parkpilotreg.UserProfile
import com.application.parkpilotreg.viewModel.UserRegisterViewModel

class UserRegister : AppCompatActivity(R.layout.user_register) {
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init view
        val imageViewProfilePicture: ImageView = findViewById(R.id.imageViewProfilePicture)
        val editTextUserName: EditText = findViewById(R.id.editTextUserName)
        val editTextFirstName: EditText = findViewById(R.id.editTextFirstName)
        val editTextLastName: EditText = findViewById(R.id.editTextLastName)
        val editTextBirthDate: EditText = findViewById(R.id.editTextBirthDate)
        val editTextAge: EditText = findViewById(R.id.editTextAge)
        val radioGroupGender: RadioGroup = findViewById(R.id.radioGroupGender)
        val buttonSave: Button = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBar)

        // getting userRegister view model reference
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserRegisterViewModel(this@UserRegister) as T
            }
        })[UserRegisterViewModel::class.java]

        // it will store MainActivity intent or null
        // why it's here? ans:- [ if user came from Main Activity then we have to throw user again to Main Activity, otherwise do nothing]
        var nextIntent: Intent? = Intent(this, MainActivity::class.java)

        viewModel.getProfileDetails()

        // get user details from user collection
        viewModel.getUserDetails()

        editTextBirthDate.setOnClickListener {
            // start and end dates format should be yyyy-mm-dd (modify this function)
            viewModel.datePicker.showDatePicker("Select Birth Date", null, null)
        }

        imageViewProfilePicture.setOnClickListener {
            // start photo picker
            viewModel.photoPicker.showPhotoPicker()
        }

        buttonSave.setOnClickListener {
            var isValid = true
            isValid = isValid(editTextUserName) and isValid
            isValid = isValid(editTextFirstName) and isValid
            isValid = isValid(editTextLastName) and isValid

            if (isValid) {
                // uploading the user data
                showProgress()
                viewModel.saveUserData(
                    this,
                    UserCollection(
                        editTextFirstName.text.toString(),
                        editTextLastName.text.toString(),
                        editTextBirthDate.text.toString(),
                        if (radioGroupGender.checkedRadioButtonId == R.id.radioButtonFemale) "female" else "male"
                    ),
                    UserProfile(
                        editTextUserName.text.toString()
                    )
                )
            }
        }

        // it is a observer of getImage method's result
        viewModel.imageLoaderResult.observe(this) { image ->
            // loaded image applying to profile picture
            imageViewProfilePicture.setImageDrawable(image.drawable)
        }

        // it will execute when fireStore result get successfully
        viewModel.userInformation.observe(this) { userCollection ->
            // set the data if user Collection is not null
            userCollection?.let {
                editTextFirstName.setText(it.firstName)
                editTextLastName.setText(it.lastName)
                editTextBirthDate.setText(it.birthDate)
                editTextAge.setText(viewModel.getAge(it.birthDate))
                radioGroupGender.check(if (it.gender == "female") R.id.radioButtonFemale else R.id.radioButtonMale)
                // if user came from home/other activity except mainActivity
                nextIntent = null
            }
        }

        viewModel.userProfile.observe(this) { userProfile ->
            userProfile?.let {
                editTextUserName.setText(userProfile.userName)
                if (userProfile.userPicture != null) {
                    imageViewProfilePicture.load(userProfile.userPicture)
                    viewModel.photoUrl = userProfile.userPicture
                } else {
                    imageViewProfilePicture.load(R.drawable.person_icon)
                }
            }
        }

        // it will execute when date picker get some date from user
        viewModel.datePicker.pickedDate.observe(this) { date ->
            // set date to birthdate editText if is not null
            date?.let {
                editTextBirthDate.setText(it)
                editTextAge.setText(viewModel.getAge(it))
            }
        }

        // it will execute when photo picker get image
        viewModel.photoPicker.pickedImage.observe(this) { imageUri ->
            // execute below code if imageUri is not null
            imageUri?.let {
                viewModel.photoUrl = it
                imageViewProfilePicture.setImageURI(it)
            }
        }

        // it will execute when user data uploaded successfully or failed to upload
        viewModel.isUploaded.observe(this) { isUploaded ->
            unShowProgress()
            if (isUploaded) {
                Toast.makeText(
                    this, "Detail save successfully", Toast.LENGTH_SHORT
                ).show()

                nextIntent?.let {
                    startActivity(nextIntent)
                }
                finish()
            } else {
                Toast.makeText(
                    this, "Failed save detail", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isValid(editText: EditText): Boolean {
        return if (editText.text.isBlank()) {
            editText.error = "Must not be blank"
            false
        } else {
            editText.error = null
            true
        }
    }

    private fun showProgress() {
        // show progress bar
        progressBar.visibility = View.VISIBLE

        // to disable user interaction with ui
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun unShowProgress() {
        // hide progress bar
        progressBar.visibility = View.GONE

        // to enable user interaction with ui
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}