package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

/**
 * Defines the current state of the CameraScreen
 */
data class CameraUiState(
    val flashMode: FlashMode,
    val expandedTopBarMode: ExpandedTopBarMode,
    val filtersMode: FiltersMode,
)

/**
 * Defines current flash mode
 */
enum class FlashMode {
    AUTO,
    ON,
    OFF
}

/**
 * Defines current filter mode
 */
enum class FiltersMode(val color: String) {
    BLUR("Blur"),
    REPLACE_BACK("Replace background"),
    BEAUTIFY("Beautification"),
    SMART_ZOOM("Smart Zoom"),
    COLOR_CORRECTION("Color correction"),
    NONE("No mode")
}

/**
 * Defines which settings are displayed on the top bar
 */
enum class ExpandedTopBarMode {
    FLASH,
    FILTERS,
    SETTINGS,
    DEFAULT
}
