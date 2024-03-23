package com.application.parkpilotreg.module

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.application.parkpilotreg.Time
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class TimePicker(message: String, timeFormat: Int) {
    private var timePicker: MaterialTimePicker
    val liveDataTimePicker = MutableLiveData<Time>()

    init {

        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(timeFormat)
            .setTitleText(message)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            liveDataTimePicker.value =
                Time(timePicker.hour, timePicker.minute)
        }
    }

    fun showTimePicker(manager: FragmentManager, tag: String?) {
        timePicker.show(manager, tag)
    }

    fun format24(time: Time): String {
        return "${time.hours} : ${time.minute}"
    }

    fun format12(time: Time): String {
        val zero = if(time.hours < 10 || (time.hours in 13..21)) "0" else ""
        if (time.hours > 12) return "$zero${time.hours - 12}:${time.minute} PM"
        return "$zero${time.hours}:${time.minute} AM"
    }

    companion object {
        const val CLOCK_12H = TimeFormat.CLOCK_12H
        const val CLOCK_24H = TimeFormat.CLOCK_24H
    }
}