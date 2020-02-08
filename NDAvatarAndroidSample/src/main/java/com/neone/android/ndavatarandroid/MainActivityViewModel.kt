package com.neone.android.ndavatarandroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val borderWidthSize = MutableLiveData(0)

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