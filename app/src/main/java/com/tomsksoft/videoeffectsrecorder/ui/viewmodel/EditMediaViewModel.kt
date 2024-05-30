package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import io.reactivex.rxjava3.core.Observable

interface EditMediaViewModel {
    fun updateSurface(surface: Surface?)
    fun addMedia(uri: String)
    fun processImage()
    val cameraConfig: Observable<CameraConfig>
    fun setPrimaryFilter(filtersMode: PrimaryFiltersMode)
}