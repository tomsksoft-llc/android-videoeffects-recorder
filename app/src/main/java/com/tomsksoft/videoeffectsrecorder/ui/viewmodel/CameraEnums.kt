package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.R

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
enum class FiltersMode(val description: Int) {
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