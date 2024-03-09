package com.tomsksoft.videoeffectsrecorder.domain

/**
 * @param background background image for {@link BackgroundMode#Replace Replace Mode}
 * @param blurPower power of blur in 0..1 for {@link BackgroundMode#Blur Blur Mode}
 * @param smartZoom auto-framing, face size in percent of frame
 * @param beautification beautification power in percent
 */
data class CameraConfig(
    val backgroundMode: BackgroundMode,
    val background: Any?,
    val blurPower: Float,
    val smartZoom: Int?,
    val beautification: Int?,
    val colorCorrection: ColorCorrection
) {
    enum class ColorCorrection {
        NO_FILTER,
        COLOR_CORRECTION,
        COLOR_GRADING,
        PRESET
    }
    enum class BackgroundMode {
        Regular,
        Remove,
        Replace,
        Blur
    }
}

val DEFAULT_CAMERA_CONFIG = CameraConfig(
    backgroundMode = CameraConfig.BackgroundMode.Regular,
    background = null,
    blurPower = 0f,
    smartZoom = null,
    beautification = null,
    colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
)