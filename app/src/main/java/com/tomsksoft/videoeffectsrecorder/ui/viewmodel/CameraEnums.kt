package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import androidx.annotation.StringRes
import com.tomsksoft.videoeffectsrecorder.R

/**
 * Defines current filter mode that can't be launched simultaneously with the others from this enum
 */
enum class PrimaryFiltersMode(@StringRes val resourceId: Int) {
    BLUR(R.string.effects_blur),
    REPLACE_BACK(R.string.effects_replace_background),
    COLOR_CORRECTION(R.string.effects_color_correction),
    NONE(R.string.effects_none)
}

/**
 * Defines current filter mode that can be launched together and over primary filters
 */
enum class SecondaryFiltersMode(@StringRes val resourceId: Int) {
    BEAUTIFY(R.string.effects_beautify),
    SMART_ZOOM(R.string.effects_smart_zoom)
}

/**
 * Defines which settings are displayed on the top bar
 */
enum class ExpandedTopBarMode {
    FLASH,
    SETTINGS,
    DEFAULT
}