package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.MainThread
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.abs

class CameraImpl @MainThread constructor(
    context: Context,
    cameraSelector: CameraSelector
): Camera, Analyzer, OnFrameAvailableListener, LifecycleOwner {
    companion object {
        private val sdkFactory = EffectsSDK.createSDKFactory()
        private val executor = Executors.newSingleThreadExecutor()
    }

    private val pipeline = sdkFactory.createImagePipeline(context)

    private val analysis = ImageAnalysis.Builder().build()
    private lateinit var processCameraProvider: ProcessCameraProvider

    private val orientationListener = OrientationEventListenerImpl(context)

    private val _frame = BehaviorSubject.create<Any>().toSerialized()
    private val _degree = BehaviorSubject.create<Int>()

    override val frame = _frame.observeOn(Schedulers.io())
    override val degree = _degree.observeOn(Schedulers.io())

    override val lifecycle = LifecycleRegistry(this)

    var cameraSelector: CameraSelector = cameraSelector
        set(value) {
            field = value
            if (isEnabled && isInitialized) {
                stop()
                skipNextFrame = true
                start()
            }
        }

    @Volatile
    override var isEnabled: Boolean = false
        set(value) {
            if (field == value) return
            if (isInitialized) if (value) start() else stop()
            field = value
        }
    @Volatile
    var isInitialized = false // CameraX process provider is initialized (ImagePipeline can be still initializing)

    private var skipNextFrame = false // workaround to get rid of upside down frames (it seems ImageAnalysis have a bug)

    init {
        pipeline.setOnFrameAvailableListener(this)
        analysis.setAnalyzer(executor, this)
        lifecycle.currentState = Lifecycle.State.CREATED

        val listenableFuture = ProcessCameraProvider.getInstance(context)
        listenableFuture.addListener({
            processCameraProvider = listenableFuture.get()
            isInitialized = true
            if (isEnabled)
                start()
        }, executor)
    }

    override fun configure(config: CameraConfig): Unit =
        pipeline.run {
            /* Background Mode */
            when (config.backgroundMode) {
                CameraConfig.BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                CameraConfig.BackgroundMode.Remove -> setMode(PipelineMode.REMOVE)
                CameraConfig.BackgroundMode.Replace -> {
                    setMode(PipelineMode.REPLACE)
                    setBackground(config.background as Bitmap)
                }
                CameraConfig.BackgroundMode.Blur -> {
                    setMode(PipelineMode.BLUR)
                    setBlurPower(config.blurPower)
                }
            }
            /* Smart Zoom */
            setZoomLevel(config.smartZoom ?: 0)
            /* Beautification */
            if (config.beautification != null) {
                enableBeautification(true)
                setBeautificationPower(config.beautification!!)
            } else enableBeautification(false)
            /* Color Correction */
            setColorCorrectionMode(when (config.colorCorrection) {
                CameraConfig.ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
                CameraConfig.ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
                CameraConfig.ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
                CameraConfig.ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
            })
        }

    // get frame from CameraX and forward it to EffectsSDK pipeline
    override fun analyze(image: ImageProxy): Unit = image.use {
        if (skipNextFrame) { // next frame after changing camera have irrelevant rotation
            skipNextFrame = false
            return
        }

        val matrix = Matrix().apply {
            preRotate(image.imageInfo.rotationDegrees.toFloat(), 0.5f, 0.5f)
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) // mirror front camera
                postScale(-1f, 1f)
        }

        var bitmap = image.toBitmap()
        bitmap = Bitmap.createBitmap( // CameraX frame is always landscape, so rotate it to portrait
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        pipeline.process(
            /*frameFactory.createARGB(bitmap)*/
            bitmap
        )
    }

    // get frame from EffectsSDK pipeline and forward it to Rx
    override fun onNewFrame(bitmap: Bitmap) {
        _frame.onNext(
            FrameMapper.toAny(bitmap)
        )
    }

    private fun start() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.RESUMED
            processCameraProvider.bindToLifecycle(
                this@CameraImpl,
                cameraSelector,
                analysis
            )
            orientationListener.enable()
        }
    }

    private fun stop() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.CREATED
            processCameraProvider.unbindAll()
            orientationListener.disable()
        }
    }

    private inner class OrientationEventListenerImpl(
        context: Context
    ): android.view.OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) // flat orientation
                return

            val degree = when {
                abs(orientation - 90) <= 45 -> 90
                abs(orientation - 180) <= 45 -> 180
                abs(orientation - 270) <= 45 -> 270
                else -> 0
            }

            if (degree != _degree.value)
                _degree.onNext(degree)
        }
    }
}