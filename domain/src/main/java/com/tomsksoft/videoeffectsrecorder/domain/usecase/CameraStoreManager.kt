package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraStore

class CameraStoreManager<T: Camera<*>>(
    private val cameraStore: CameraStore<T>
) {
    val cameras: Array<T>
        get() = cameraStore.availableCameras
    val camerasCount: Int
        get() = cameraStore.availableCameras.size
}