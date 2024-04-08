package com.tomsksoft.videoeffectsrecorder.domain

/**
 * @param background background image for {@link BackgroundMode#Replace Replace Mode}
 * @param blurPower power of blur in 0..1 for {@link BackgroundMode#Blur Blur Mode}
 * @param smartZoom auto-framing, face size in percent of frame
 * @param beautification beautification power in percent
 */
data class CameraConfig(
    val backgroundMode: BackgroundMode = BackgroundMode.Regular,
    val background: Any? = null,
    val blurPower: Float = 0f,
    val smartZoom: Int? = null,
    val beautification: Int? = null,
    val colorCorrection: ColorCorrection = ColorCorrection.NO_FILTER,
    val colorCorrectionPower: Float = 0f,
    val colorGradingReference: Any? = null
)