package com.tomsksoft.videoeffectsrecorder.domain

data class CameraConfig(
    val backgroundMode: BackgroundMode,
    val smartZoom: SmartZoom,
    val isBeautificationEnabled: Boolean,
    val colorCorrection: ColorCorrection
) {
    enum class ColorCorrection {
        NO_FILTER,
        COLOR_CORRECTION,
        COLOR_GRADING,
        PRESET_MODE
    }

    sealed interface BackgroundMode {
        // object Remove: BackgroundMode

        class Replace: BackgroundMode // TODO [tva] store background image or video

        data class Blur(val radius: Float, val quality: Float): BackgroundMode {
            init {
                require(radius in 0f..1f) { "radius must be in [0..1] but was $radius" }
                require(quality in 0f..1f) { "quality must be in [0..1] but was $quality" }
            }
        }
    }

    /**
     * @param faceSize face size in percent of the frame
     */
    data class SmartZoom(val faceSize: Int)
}