package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import com.tomsksoft.videoeffectsrecorder.data.MediaProcessorImpl
import com.tomsksoft.videoeffectsrecorder.domain.entity.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.usecase.EditorManager
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditMediaViewModelImpl @Inject constructor(
    private val editorManager: EditorManager,
    private val mediaProcessor: MediaProcessorImpl
): ViewModel(), EditMediaViewModel {

    override val cameraConfig = editorManager.cameraConfig

    override fun updateSurface(surface: Surface?) {
        mediaProcessor.setSurface(surface)
    }

    override fun addMedia(uri: String) {
        editorManager.addMedia(uri)
    }

    override fun processImage() {
        editorManager.processMedia()
    }

    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        Log.d("EditMediaViewModel", "$filtersMode")
        val currentCameraConfig = cameraConfig.blockingFirst()
        when (filtersMode) {
            PrimaryFiltersMode.BLUR -> editorManager.setCameraConfig(
                currentCameraConfig.copy(
                    backgroundMode = BackgroundMode.Blur,
                    blurPower = 0.9f
                )
            )
            PrimaryFiltersMode.REPLACE_BACK -> editorManager.setCameraConfig(
                currentCameraConfig.copy(
                    backgroundMode =
                    if (currentCameraConfig.background == null)
                        BackgroundMode.Remove
                    else BackgroundMode.Replace
                )
            )
            PrimaryFiltersMode.COLOR_CORRECTION -> editorManager.setCameraConfig(
                currentCameraConfig.copy(
                    backgroundMode = BackgroundMode.Regular,
                    colorCorrection = ColorCorrection.NO_FILTER
                )
            )
            PrimaryFiltersMode.NONE -> editorManager.setCameraConfig(
                currentCameraConfig.copy(
                    backgroundMode = BackgroundMode.Regular,
                    colorCorrection = ColorCorrection.NO_FILTER
                )
            )
        }
    }
}