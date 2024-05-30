package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.core.net.toUri
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.ImagePipeline
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaProcessor
import com.tomsksoft.videoeffectsrecorder.domain.entity.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection

class MediaProcessorImpl(
    private val context: Context
): MediaProcessor {
    companion object {
        private const val TAG = "MediaProcessorImpl"
        private val factory = EffectsSDK.createSDKFactory()
    }

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    private var image: Bitmap? = null
    private var pipeline: ImagePipeline = createPipeline()
    private var surface: Surface? = null

    private fun createPipeline() = factory.createImagePipeline(context)

    override fun addMedia(uri: String) {
        if (uri.contains("video")) {

        } else if (uri.contains("image")) {
            //load image from external storage
            Log.d(TAG, uri)
            image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri.toUri()))
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri.toUri())
            }
        }
    }

    fun setSurface(surface: Surface?) {
        this.surface = surface
        pipeline.setOutputSurface(this.surface)
        //pipeline.process(image!!.copy(Bitmap.Config.ARGB_8888, true))
    }
    override fun processImage() {
        pipeline.process(image!!.copy(Bitmap.Config.ARGB_8888, true))
    }

    override fun configure(cameraConfig: CameraConfig) {
        Log.d(TAG, "$cameraConfig")
        pipeline.run {
            /* Background Mode */
            when (cameraConfig.backgroundMode) {
                BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                BackgroundMode.Remove -> setMode(PipelineMode.REMOVE)
                BackgroundMode.Replace -> {
                    setMode(PipelineMode.REPLACE)
                    setBackground(cameraConfig.background as Bitmap)
                }
                BackgroundMode.Blur -> {
                    setMode(PipelineMode.BLUR)
                    setBlurPower(cameraConfig.blurPower)
                }
            }
            /* Smart Zoom */
            setZoomLevel(cameraConfig.smartZoom ?: 0)
            /* Beautification */
            if (cameraConfig.beautification != null) {
                enableBeautification(true)
                setBeautificationPower(cameraConfig.beautification!!)
            } else enableBeautification(false)
            /* Color Correction */
            setColorCorrectionMode(when (cameraConfig.colorCorrection) {
                ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
                ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
                ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
                ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
            })
            if (
                cameraConfig.colorCorrection == ColorCorrection.COLOR_GRADING &&
                cameraConfig.colorGradingSource != null
            ) setColorGradingReferenceImage(cameraConfig.colorGradingSource as Bitmap)
            setColorFilterStrength(cameraConfig.colorCorrectionPower)
            /* Sharpness */
            setSharpeningStrength(cameraConfig.sharpnessPower ?: 0f)
        }
    }
}