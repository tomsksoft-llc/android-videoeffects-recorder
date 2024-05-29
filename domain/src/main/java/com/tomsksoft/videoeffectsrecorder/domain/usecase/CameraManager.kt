package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.Direction
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraManager {
    private val _orientation = BehaviorSubject.createDefault(0)
    private val _direction = BehaviorSubject.createDefault(Direction.FRONT)
    private val _flashMode = BehaviorSubject.createDefault(FlashMode.AUTO)
    private val _isFlashEnabled = BehaviorSubject.createDefault(false)
    private val _cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    val orientation = _orientation.distinctUntilChanged()
    val direction = _direction.distinctUntilChanged()
    val flashMode = _flashMode.distinctUntilChanged()
    val isFlashEnabled = _isFlashEnabled.distinctUntilChanged().map {
        when (flashMode.blockingFirst()) { // prevent flash state being inconsistent with its mode
            FlashMode.ON -> true
            FlashMode.OFF -> false
            FlashMode.AUTO -> it
        }
    }
    val cameraConfig = _cameraConfig.distinctUntilChanged()

    init {
        flashMode.map { mode -> // flash mode automatically updates flash state
            when (mode) {
                FlashMode.ON -> true
                FlashMode.OFF -> false
                FlashMode.AUTO -> false
            }
        }.subscribe(_isFlashEnabled)
    }

    fun setOrientation(orientation: Int) = _orientation.onNext(orientation)
    fun setDirection(direction: Direction) = _direction.onNext(direction)
    fun setFlashMode(flashMode: FlashMode) = _flashMode.onNext(flashMode)
    fun setFlashEnabled(isEnabled: Boolean) = _isFlashEnabled.onNext(isEnabled)
    fun setCameraConfig(cameraConfig: CameraConfig) = _cameraConfig.onNext(cameraConfig)
}