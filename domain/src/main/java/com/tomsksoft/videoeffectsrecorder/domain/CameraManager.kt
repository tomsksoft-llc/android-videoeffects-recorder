package com.tomsksoft.videoeffectsrecorder.domain

import android.util.Log
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.Timer
import java.util.TimerTask

class CameraManager(
    private val camera: Camera,
    private val frameProcessor: FrameProcessor
): AutoCloseable {
    val frameSource by frameProcessor::processedFrame
    val orientation by camera::orientation
    var direction by camera::direction
    val cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    private val disposables = arrayOf(
        cameraConfig.subscribe(frameProcessor::configure),
        frameSource.subscribe { framesCount++ }
    )

    /* FPS counter for debug purposes */
    @Volatile private var framesCount: Int = 0
    @Volatile private var fps: Float = 0f
    private val timer = Timer().apply {
        val period = 3000L
        schedule(object: TimerTask() {
            override fun run() {
                fps = framesCount * 1000f / period
                framesCount = 0
                Log.d("FPS", fps.toString())
            }
        }, period, period)
    }

    init {
        camera.frameSource.subscribe(frameProcessor.frameSource)
    }

    override fun close() {
        disposables.forEach(Disposable::dispose)
        timer.cancel()
    }
}