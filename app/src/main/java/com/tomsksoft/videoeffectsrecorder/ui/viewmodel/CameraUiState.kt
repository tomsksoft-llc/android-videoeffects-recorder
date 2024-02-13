package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode,
    val expandedTopBarMode: ExpandedTopBarMode,
    val filtersMode: FiltersMode,
    val isVideoRecording: Boolean,
)