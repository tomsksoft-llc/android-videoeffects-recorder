package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode = FlashMode.OFF,
    val expandedTopBarMode: ExpandedTopBarMode = ExpandedTopBarMode.DEFAULT,
    val primaryFiltersMode: PrimaryFiltersMode = PrimaryFiltersMode.NONE,
    val smartZoom: Int? = null,
    val beautification: Int? = null,
    val blur: Float = 0.125f,
    val colorCorrection: ColorCorrection = ColorCorrection.NO_FILTER,
    val isVideoRecording: Boolean = false,
    val isCameraInitialized: Boolean = true,
    val pipelineCameraDirection: Camera.Direction = Camera.Direction.BACK
)