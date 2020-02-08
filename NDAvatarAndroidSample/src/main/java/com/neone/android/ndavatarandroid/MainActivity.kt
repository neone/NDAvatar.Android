package com.neone.android.ndavatarandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private val mainActViewModel by lazy {MainActivityViewModel(application)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeVM()

        // Use LiveData to update border size display AND
        // Set the border stroke width size
        mainActViewModel.borderWidthSize.observe(this, Observer<Int> {newSizeValue ->
            borderSizeDisplay.text = newSizeValue.toString()
            circleImageView.setBorderWidth(newSizeValue)
        })

        // Use live data to update avatar border stroke length automatically
        mainActViewModel.borderStrokeColor.observe(this, Observer<Int> {newColorIntValue ->
            circleImageView.setBorderStrokeColor(newColorIntValue)
        })

        // Seekbar handlers being set
        seekBar_borderSizeSlider.setOnSeekBarChangeListener(this)
        borderColorControls_slider_red.setOnSeekBarChangeListener(this)
        borderColorControls_slider_green.setOnSeekBarChangeListener(this)
        borderColorControls_slider_blue.setOnSeekBarChangeListener(this)

        // Button handlers being set
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

    /**
     * Seekbar handler
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Match action to specific seekBar with a progress change
        when (seekBar?.id) {
            seekBar_borderSizeSlider.id -> {
                mainActViewModel.borderWidthSize.value = seekBar.progress
            }
            borderColorControls_slider_red.id -> {
                mainActViewModel.borderStrokeColorRedChannel = seekBar.progress
            }
            borderColorControls_slider_green.id -> {
                mainActViewModel.borderStrokeColorGreenChannel = seekBar.progress
            }
            borderColorControls_slider_blue.id -> {
                mainActViewModel.borderStrokeColorBlueChannel = seekBar.progress
            }
            else -> {
                Log.d("test", "Seekbar with ID: ${seekBar?.id} not found")
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}


