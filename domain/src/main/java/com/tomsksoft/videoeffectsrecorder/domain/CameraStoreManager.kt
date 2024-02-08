package com.tomsksoft.videoeffectsrecorder.domain

class CameraStoreManager<T: Camera<*>>(
    private val cameraStore: CameraStore<T>
) {
    val cameras: Array<T>
        get() = cameraStore.availableCameras
    val camerasCount: Int
        get() = cameraStore.availableCameras.size
}