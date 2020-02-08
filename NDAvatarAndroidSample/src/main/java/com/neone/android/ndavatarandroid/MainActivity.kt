package com.neone.android.ndavatarandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val mainActViewModel by lazy {MainActivityViewModel(application)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeVM()

        // Use LiveData to update border size display AND
        // Set the border size
        mainActViewModel.borderWidthSize.observe(this, Observer<Int> {newSizeValue ->
            borderSizeDisplay.text = newSizeValue.toString()
            circleImageView.setBorderWidth(newSizeValue)
        })

        btn_borderSizeIncrease.setOnClickListener { handleBorderSizeIncreaseClick() }
        btn_borderSizeDecrease.setOnClickListener { handleBorderSizeDecreaseClick() }
    }

    /**
     * Used to seed View-specific values into the VM.
     * The VM must not contain references to the View, Application, or Context.
     */
    private fun initializeVM() {
        // Border width
        mainActViewModel.borderWidthSize.value = circleImageView?.getBorderWidth() ?: 0
    }

    private fun handleBorderSizeDecreaseClick() {
        mainActViewModel.decreaseBorderWidthSize()
    }

    private fun handleBorderSizeIncreaseClick() {
        mainActViewModel.increaseBorderWidthSize()
    }
}


