package com.tomsksoft.videoeffectsrecorder.domain.usecase

import android.util.Log
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaProcessor
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class EditorManager (
    private val mediaProcessor: MediaProcessor
) {
    private val _cameraConfig = BehaviorSubject.create<CameraConfig>()
    val cameraConfig: Observable<CameraConfig>
        get() = _cameraConfig.distinctUntilChanged()

    val disposable = _cameraConfig.subscribe(mediaProcessor::configure)

    fun addMedia(uri: String) {
        mediaProcessor.addMedia(uri)
    }

    fun processMedia() {
        mediaProcessor.processImage()
    }

    fun setCameraConfig(cameraConfig: CameraConfig){
        Log.d("EditorManager", "$cameraConfig")
        _cameraConfig.onNext(cameraConfig)
    }
}