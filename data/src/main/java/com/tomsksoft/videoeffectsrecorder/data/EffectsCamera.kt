package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.CameraPipeline
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.VideoConfig
import java.util.LinkedList

class EffectsCamera(context: Context): Camera<Frame> {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    init {
        EffectsSDK.initialize(context) // can it be invoked several times?
    }

    private val pipelineBuilder = factory
        .createCameraPipelineBuilder()
        .setContext(context)
    private lateinit var pipeline: CameraPipeline
    private val subscribers = LinkedList<Camera.OnFrameListener<Frame>>()

    override var isEnabled: Boolean = false // starts or stops the camera on change
        set(value) {
            if (field == value) return
            if (value) start() else stop()
            field = value
        }

    override fun configure(config: VideoConfig) {
        pipelineBuilder // TODO [tva] setup builder
    }

    override fun subscribe(listener: Camera.OnFrameListener<Frame>) {
        subscribers += listener
    }

    private fun start() {
        pipeline = pipelineBuilder.build().apply {
            startPipeline()
            setOnFrameAvailableListener(this@EffectsCamera::onNewFrame)
        }
    }

    private fun stop() = Unit // TODO [tva] stop pipeline

    private fun onNewFrame(bitmap: Bitmap) =
        subscribers.forEach { it.onFrame(Frame(bitmap)) }
}