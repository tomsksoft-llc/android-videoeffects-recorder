package com.tomsksoft.videoeffectsrecorder.data

import android.app.Activity
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.graphics.rotationMatrix
import androidx.lifecycle.LifecycleOwner
import com.effectssdk.tsvb.frame.factory.FrameFactoryImpl
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.ImagePipelineImpl
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.Executors

class CameraImpl(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Activity,
    cameraSelector: CameraSelector
): Camera<Frame>, Analyzer, OnFrameAvailableListener {
    companion object {
        private val frameFactory = FrameFactoryImpl()
        private val executor = Executors.newSingleThreadExecutor()
        private val analysis = ImageAnalysis.Builder().build()
    }

    private val pipeline = ImagePipelineImpl.Builder()
        .setContext(context)
        .build()
    private lateinit var processCameraProvider: ProcessCameraProvider

    private val _frame = BehaviorSubject.create<Frame>().toSerialized()
    private val _degree = BehaviorSubject.create<Int>().toSerialized()

    override val frame: Observable<Frame>
        get() = _frame.observeOn(Schedulers.io())
    override val degree: Observable<Int>
        get() = _degree.observeOn(Schedulers.io())

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
                is CameraConfig.BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                is CameraConfig.BackgroundMode.Remove -> setMode(PipelineMode.REMOVE)
                is CameraConfig.BackgroundMode.Replace -> {
                    setMode(PipelineMode.REPLACE)
                    setBackground((config.backgroundMode as CameraConfig.BackgroundMode.Replace).bitmap)
                }
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

    // get frame from CameraX and forward it to EffectsSDK pipeline
    override fun analyze(image: ImageProxy): Unit = image.use {
        if (skipNextFrame) { // next frame after changing camera have irrelevant rotation
            skipNextFrame = false
            return
        }

        var bitmap = image.toBitmap() // CameraX frame is always landscape
        bitmap = Bitmap.createBitmap( // rotate to portrait orientation
            bitmap,
            0, 0, bitmap.width, bitmap.height,
            rotationMatrix(
                image.imageInfo.rotationDegrees.toFloat(),
                0.5f, 0.5f
            ), true
        )
        pipeline.process(
            frameFactory.createARGB(bitmap)
        )
    }

    // get frame from EffectsSDK pipeline and forward it to Rx
    override fun onNewFrame(bitmap: Bitmap) {
        _frame.onNext(Frame(bitmap))
    }

    private fun start() {
        context.runOnUiThread {
            processCameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, analysis)
        }
    }

    private fun stop() {
        context.runOnUiThread {
            processCameraProvider.unbindAll()
        }
    }
}