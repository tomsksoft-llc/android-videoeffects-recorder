package com.tomsksoft.videoeffectsrecorder.data

import android.app.Activity
import android.util.Log
import com.effectssdk.tsvb.EffectsSDK
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.OnFrameListener

class CameraImpl(
    context: Activity,
    camera: com.effectssdk.tsvb.Camera
): Camera<Frame> {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    init { EffectsSDK.initialize(context) }

    internal val pipeline = factory.createCameraPipelineBuilder()
        .setContext(context)
        .setCamera(camera)
        .build()

    private val subscribers = ArrayList<OnFrameListener<Frame>>()

    override var isEnabled: Boolean = false
        set(value) {
            if (field == value) return
            if (value) enable() else disable()
            field = value
        }

    override fun subscribe(listener: OnFrameListener<Frame>) {
        subscribers += listener
    }

    override fun unsubscribe(listener: OnFrameListener<Frame>) {
        subscribers -= listener
    }

    private fun enable() {
        pipeline.startPipeline()
        pipeline.setOnFrameAvailableListener { bitmap ->
            subscribers.forEach { it.onFrame(Frame(bitmap)) }
        }
        pipeline.setOrientationChangeListener { orientation, rotation ->
            Log.d("Camera", "$orientation $rotation") // TODO [tva] notify UI to rotate icons
        }
    }

    private fun disable() {
        pipeline.release()
    }
}