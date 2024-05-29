package com.tomsksoft.videoeffectsrecorder.ui.entity

import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.entity.Direction
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode = FlashMode.OFF,
    val primaryFiltersMode: PrimaryFiltersMode = PrimaryFiltersMode.NONE,
    val smartZoom: Int? = null,
    val beautification: Int? = null,
    val blur: Float = 0.125f,
    val colorCorrectionMode: ColorCorrection = ColorCorrection.NO_FILTER,
    val isVideoRecording: Boolean = false,
    val isCameraInitialized: Boolean = true,
    val pipelineCameraDirection: Direction = Direction.BACK,
    val colorCorrectionPower: Float = 0.125f,
    val sharpnessPower: Float? = null
)