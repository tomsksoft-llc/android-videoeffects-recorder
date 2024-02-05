package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CameraViewModel: ViewModel() {
	val flashMode = mutableStateOf(FlashMode.AUTO)

    fun changeFlashMode() {
        val values = FlashMode.values()
        flashMode.value = values[(values.indexOf(flashMode.value) + 1) % values.size] // next mode
    }

    enum class FlashMode {
        AUTO,
        ON,
        OFF
    }
}