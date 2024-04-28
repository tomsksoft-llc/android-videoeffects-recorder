package com.tomsksoft.videoeffectsrecorder.ui

import android.content.res.Resources
import com.tomsksoft.videoeffectsrecorder.domain.FlashMode

fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun FlashMode.getNextFlashMode(): FlashMode {
    return when (this) {
        FlashMode.OFF -> FlashMode.AUTO
        FlashMode.AUTO -> FlashMode.ON
        FlashMode.ON -> FlashMode.OFF
    }
}