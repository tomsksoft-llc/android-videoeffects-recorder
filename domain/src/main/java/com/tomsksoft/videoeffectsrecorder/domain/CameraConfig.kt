package com.tomsksoft.videoeffectsrecorder.domain

/**
 * @param background background image for {@link BackgroundMode#Replace Replace Mode}
 * @param blurPower power of blur in 0..1 for {@link BackgroundMode#Blur Blur mode}
 * @param smartZoom auto-framing, face size in percent of frame
 * @param beautification beautification power in percent
 * @param colorGradingSource image from which color filter will be generated for {@link ColorCorrection#COLOR_GRADING Color Grading mode}
 */
data class CameraConfig(
    val backgroundMode: BackgroundMode = BackgroundMode.Regular,
    val background: Any? = null,
    val blurPower: Float = 0f,
    val smartZoom: Int? = null,
    val beautification: Int? = null,
    val colorCorrection: ColorCorrection = ColorCorrection.NO_FILTER,
    val colorGradingSource: Any? = null
)