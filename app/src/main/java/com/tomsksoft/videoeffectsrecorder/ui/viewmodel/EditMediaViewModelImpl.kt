package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import androidx.lifecycle.ViewModel
import com.tomsksoft.videoeffectsrecorder.domain.usecase.EditorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditMediaViewModelImpl @Inject constructor(
    val editorManager: EditorManager
): ViewModel(), EditMediaViewModel {
    override fun updateSurface(surface: Surface?) {
        editorManager.mediaProcessor.setSurface(surface)
    }

    override fun addMedia(uri: String) {
        editorManager.addMedia(uri)
    }

    override fun processImage() {
        editorManager.mediaProcessor.processImage()
    }
}