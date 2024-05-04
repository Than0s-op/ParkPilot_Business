package com.application.parkpilotreg.module

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.util.Locale

class DatePicker(startDate: Long, endDate: Long) {

    private val constraintsBuilder = CalendarConstraints.Builder()
        .setStart(startDate)
        .setEnd(endDate)
        .build()

    // just init date picker (not build)
    private var datePicker =
        MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintsBuilder).build()

    // creating a date format
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    // to observe date, hence other classes can access date, when it will change
    var pickedDate = MutableLiveData<String?>()

    init {
        // when date picker will gone
        datePicker.addOnPositiveButtonClickListener {
            // date has been selected
            if (datePicker.selection != null) {

                // converting selected date into string and set to the data variable
                pickedDate.value = simpleDateFormat.format(datePicker.selection)
            }
        }
    }

    fun show(activity: AppCompatActivity) {

        // show the date picker
        datePicker.show(activity.supportFragmentManager, null)
    }
}