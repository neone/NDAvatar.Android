package com.neone.android.ndavatarandroid

import android.app.Application
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val borderWidthSize = MutableLiveData(0)
    val borderStrokeAlphaMask = 255
    var borderStrokeColorRedChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBorderStrokeColor()
        }
    var borderStrokeColorGreenChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBorderStrokeColor()
        }
    var borderStrokeColorBlueChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBorderStrokeColor()
        }
    val borderStrokeColor = MutableLiveData(Color.argb(
        1,
        borderStrokeColorRedChannel,
        borderStrokeColorGreenChannel,
        borderStrokeColorBlueChannel
    ))

    private fun updateBorderStrokeColor() {
        borderStrokeColor.postValue(Color.argb(
            borderStrokeAlphaMask,
            borderStrokeColorRedChannel,
            borderStrokeColorGreenChannel,
            borderStrokeColorBlueChannel
        ))
    }

    fun decreaseBorderWidthSize() {
        borderWidthSize.value?.let {
            borderWidthSize.value = it - 1
        }
    }

    fun increaseBorderWidthSize() {
        borderWidthSize.value?.let {
            borderWidthSize.value = it + 1
        }
    }
}