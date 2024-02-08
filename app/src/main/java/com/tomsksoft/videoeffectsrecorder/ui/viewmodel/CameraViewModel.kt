package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.VideoEffectsRecorderApplication
import com.tomsksoft.videoeffectsrecorder.data.EffectsCamera
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraViewModel(app: VideoEffectsRecorderApplication): AndroidViewModel(app) {

    val flashMode = mutableStateOf(FlashMode.AUTO)
    val frame = mutableStateOf<Bitmap?>(null)

    private val context: Context
        get() = getApplication()
    private val camera: Camera<Frame> = EffectsCamera(context).apply {
        configure(CameraConfig())
        subscribe { onNewFrame(it.bitmap) }
        isEnabled = true
    }

    fun changeFlashMode() {
        val values = FlashMode.values()
        flashMode.value = values[(values.indexOf(flashMode.value) + 1) % values.size] // next mode
    }

    private fun onNewFrame(bitmap: Bitmap) =
        viewModelScope.launch(Dispatchers.Main) { frame.value = bitmap }

    enum class FlashMode {
        AUTO,
        ON,
        OFF
    }
}