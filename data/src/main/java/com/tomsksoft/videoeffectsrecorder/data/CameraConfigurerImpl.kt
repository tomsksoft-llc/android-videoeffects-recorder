package com.tomsksoft.videoeffectsrecorder.data

import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfigurer

class CameraConfigurerImpl: CameraConfigurer<CameraImpl> {
    override fun configure(camera: CameraImpl, config: CameraConfig) {
        camera.pipeline.apply {
            /* Background Mode */
            when (config.backgroundMode) {
                is CameraConfig.BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                is CameraConfig.BackgroundMode.Replace -> TODO("[tva] background replace mode")
                is CameraConfig.BackgroundMode.Blur -> {
                    setMode(PipelineMode.BLUR)
                    setBlurPower((config.backgroundMode as CameraConfig.BackgroundMode.Blur).power)
                }
            }
            /* Smart Zoom */
            setZoomLevel(config.smartZoom?.faceSize ?: 0)
            /* Beautification */
            if (config.beautification != null) {
                enableBeautification(true)
                setBeautificationPower(config.beautification!!.power)
            } else enableBeautification(false)
            /* Color Correction */
            setColorCorrectionMode(when (config.colorCorrection) {
                CameraConfig.ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
                CameraConfig.ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
                CameraConfig.ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
                CameraConfig.ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
            })
        }
    }
}