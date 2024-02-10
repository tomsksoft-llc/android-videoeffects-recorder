package com.tomsksoft.videoeffectsrecorder.domain

interface CameraStore<T: Camera<*>> {
    val availableCameras: Array<T>
}