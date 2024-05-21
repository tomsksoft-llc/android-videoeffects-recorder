package com.tomsksoft.videoeffectsrecorder.ui.entity

import androidx.annotation.StringRes
import com.tomsksoft.videoeffectsrecorder.R

/**
 * Defines current filter mode that can be launched together and over primary filters
 */
enum class SecondaryFiltersMode(@StringRes val resourceId: Int) {
    BEAUTIFY(R.string.effects_beautify),
    SMART_ZOOM(R.string.effects_smart_zoom),
    SHARPNESS(R.string.effects_sharpness)
}