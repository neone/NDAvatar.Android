//File         : MainActivityViewModel
//Version      : 0.1
//License      : MIT
//Author       : Marc Aldrich
//Date Created : 2020 Feb 09
//Last Modified: 2020 Feb 11
//Project Link : https://github.com/neone/NDAvatarAndroid
//Summary      : Sample application used to exercise and provide example usages for NDAvatarView project.
//Notes        :
package com.neone.android.ndavatarandroid

import android.app.Application
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * BORDER COLOR AND WIDTH VARIABLES
     */
    val borderWidthSize = MutableLiveData(0)
    var borderStrokeAlphaMask = 255
    /**
     * Updating any of these channels will post a value back to the Activity so that the view can be updated
     */
    var borderStrokeColorRedChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBorderStrokeColor()
        }
    var borderStrokeColorGreenChannel = 90
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

    /**
     * Updating any of these channels will post a value back to the Activity so that the view can be updated
     */
    var backgroundFillAlphaMask = 255
        set(value) {
            if (field == value) return
            field = value
            updateBackgroundFillColor()
        }
    var backgroundFillColorRedChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBackgroundFillColor()
        }
    var backgroundFillColorGreenChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBackgroundFillColor()
        }
    var backgroundFillColorBlueChannel = 127
        set(value) {
            if (field == value) return
            field = value
            updateBackgroundFillColor()
        }
    val backgroundFillColor = MutableLiveData(Color.argb(
        1,
        backgroundFillColorRedChannel,
        backgroundFillColorGreenChannel,
        backgroundFillColorBlueChannel
    ))

    private fun updateBackgroundFillColor() {
        backgroundFillColor.postValue(Color.argb(
            backgroundFillAlphaMask,
            backgroundFillColorRedChannel,
            backgroundFillColorGreenChannel,
            backgroundFillColorBlueChannel
        ))
    }
}