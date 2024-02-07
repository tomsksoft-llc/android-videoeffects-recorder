package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _cameraUiState : MutableStateFlow<CameraUiState>
        = MutableStateFlow(CameraUiState(
            flashMode = FlashMode.AUTO,
            expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
            filtersMode = FiltersMode.NONE)
        )

    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState

    val frame = mutableStateOf<Bitmap?>(null)

    fun setFlash(flashMode: FlashMode) {
        viewModelScope.launch {
            _cameraUiState.emit(
                cameraUiState.value.copy(
                    flashMode = flashMode
                )
            )

            // TODO [fmv]: add usecase interaction
        }
    }

    fun setFilters(filtersMode: FiltersMode) {
        viewModelScope.launch {
            _cameraUiState.emit(
                cameraUiState.value.copy(
                    filtersMode = filtersMode
                )
            )

            // TODO [fmv]: add usecase interaction
        }
    }

    fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode){
        viewModelScope.launch {
            _cameraUiState.emit(
                cameraUiState.value.copy(
                    expandedTopBarMode = expandedTopBarMode
                )
            )
        }
    }

    fun flipCamera(){
        // TODO: [fmv] add usecase interaction
    }

    fun captureImage(){
        // TODO: [fmv] add usecase interaction
    }

    fun startVideoRecording(){
        // TODO: [fmv] add usecase interaction
    }

    fun stopVideoRecording() {
        // TODO: [fmv] add usecase interaction
    }
    private fun onNewFrame(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.Main) { frame.value = bitmap }
}