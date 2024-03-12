package com.tomsksoft.videoeffectsrecorder.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 *
 */
class PipelineConfigManager(
    val camera: Camera
) {
    private val _cameraConfig = MutableStateFlow(CameraConfig())
    val cameraConfig: StateFlow<CameraConfig> = _cameraConfig.asStateFlow()

    fun configurePipeline(
        backgroundMode: CameraConfig.BackgroundMode? = null,
        background: Any? = null,
        blurPower: Float? = null,
        smartZoom: Int? = null,
        beautification: Int? = null,
        colorCorrection: CameraConfig.ColorCorrection? = null
    ) {
        val prevCameraConfig = cameraConfig.value.copy()
        _cameraConfig.update { cameraConfig ->
            cameraConfig.copy(
                backgroundMode = backgroundMode ?: prevCameraConfig.backgroundMode,
                background = background ?: prevCameraConfig.background,
                blurPower = blurPower ?: prevCameraConfig.blurPower,
                smartZoom = smartZoom ?: prevCameraConfig.smartZoom,
                beautification = beautification ?: prevCameraConfig.beautification,
                colorCorrection = colorCorrection ?: prevCameraConfig.colorCorrection
            )
        }
        camera.configure(cameraConfig.value)
    }
}