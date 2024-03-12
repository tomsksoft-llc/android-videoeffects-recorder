package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.DEFAULT_CAMERA_CONFIG

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode = FlashMode.OFF,
    val expandedTopBarMode: ExpandedTopBarMode = ExpandedTopBarMode.DEFAULT,
    val primaryFiltersMode: PrimaryFiltersMode = PrimaryFiltersMode.NONE,
    val isSmartZoomEnabled: Boolean = false,
    val isBeautifyEnabled: Boolean = false,
    val isVideoRecording: Boolean = false,
    val isCameraInitialized: Boolean = true,
)