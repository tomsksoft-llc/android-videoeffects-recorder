package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode,
    val expandedTopBarMode: ExpandedTopBarMode,
    val primaryFiltersMode: PrimaryFiltersMode,
    val isSmartZoomEnabled: Boolean,
    val isBeautifyEnabled: Boolean,
    val isVideoRecording: Boolean,
    val isCameraInitialized: Boolean
)