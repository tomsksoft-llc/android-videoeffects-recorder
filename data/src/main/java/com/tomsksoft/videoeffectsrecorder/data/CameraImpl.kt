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
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.abs

class CameraImpl @MainThread constructor(
    context: Context,
    direction: Camera.Direction
): Camera, Analyzer, LifecycleOwner {
    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }

    private val analysis = ImageAnalysis.Builder().build()
    private lateinit var processCameraProvider: ProcessCameraProvider

    private val orientationListener = OrientationEventListenerImpl(context)

    override val frameSource = BehaviorSubject.create<Any>().toSerialized()
    override var orientation: Int = 0

    override val lifecycle = LifecycleRegistry(this)

    override var direction: Camera.Direction = direction
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

    override fun analyze(image: ImageProxy): Unit = image.use {
        if (skipNextFrame) { // next frame after changing camera have irrelevant rotation
            skipNextFrame = false
            return
        }

        val matrix = Matrix().apply {
            preRotate(image.imageInfo.rotationDegrees.toFloat(), 0.5f, 0.5f)
            if (direction == Camera.Direction.FRONT) // mirror front camera
                postScale(-1f, 1f)
        }

        var bitmap = image.toBitmap()
        bitmap = Bitmap.createBitmap( // CameraX frame is always landscape, so rotate it to portrait
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        frameSource.onNext(FrameMapper.toAny(bitmap))
    }

    private fun start() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.RESUMED
            processCameraProvider.bindToLifecycle(
                this@CameraImpl,
                when (direction) {
                    Camera.Direction.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                    Camera.Direction.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                },
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

            if (degree != orientation)
                this@CameraImpl.orientation = degree
        }
    }
}