package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import io.reactivex.rxjava3.core.Observable

object EditMediaViewModelStub: EditMediaViewModel {
    override fun updateSurface(surface: Surface?) {
        TODO("Not yet implemented")
    }

    override fun addMedia(uri: String) {
        TODO("Not yet implemented")
    }

    override fun processImage() {
        TODO("Not yet implemented")
    }

    override val cameraConfig: Observable<CameraConfig>
            get() = TODO("Not yet implemented")

    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        TODO("Not yet implemented")
    }
}