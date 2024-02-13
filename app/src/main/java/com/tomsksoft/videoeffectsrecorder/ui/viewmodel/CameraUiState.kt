package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import androidx.annotation.StringRes
import com.tomsksoft.videoeffectsrecorder.R

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
enum class FiltersMode(@StringRes val description: Int) {
    BLUR(R.string.effects_blur),
    REPLACE_BACK(R.string.effects_replace_background),
    BEAUTIFY(R.string.effects_beautify),
    SMART_ZOOM(R.string.effects_smart_zoom),
    COLOR_CORRECTION(R.string.effects_color_correction),
    NONE(R.string.effects_none)
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
