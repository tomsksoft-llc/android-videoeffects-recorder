package com.tomsksoft.videoeffectsrecorder.domain

import android.graphics.Bitmap

/**
 * @param smartZoom auto-framing, set null to disable
 * @param beautification set null to disable
 */
data class CameraConfig(
    val backgroundMode: BackgroundMode,
    val smartZoom: SmartZoom?,
    val beautification: Beautification?,
    val colorCorrection: ColorCorrection
) {
    enum class ColorCorrection {
        NO_FILTER,
        COLOR_CORRECTION,
        COLOR_GRADING,
        PRESET
    }

    sealed interface BackgroundMode {
        object Regular: BackgroundMode

        object Remove: BackgroundMode

        class Replace(val bitmap: Bitmap): BackgroundMode

        data class Blur(val power: Double): BackgroundMode {
            init {
                require(power in 0f..1f) { "power must be in [0..1] but was $power" }
            }
        }
    }

    /**
     * @param faceSize face size in percent of the frame
     */
    data class SmartZoom(val faceSize: Int) {
        init {
            require(faceSize in 0..100) { "faceSize must be in [0..100] but was $faceSize" }
        }
    }

    /**
     * @param power beautification power in percent
     */
    data class Beautification(val power: Int) {
        init {
            require(power in 0..100) { "power must be in [0..100] but was $power" }
        }
    }
}