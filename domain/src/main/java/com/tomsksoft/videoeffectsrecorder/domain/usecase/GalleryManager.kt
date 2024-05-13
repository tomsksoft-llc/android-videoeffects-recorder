package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
import io.reactivex.rxjava3.core.Observable

class GalleryManager(
    private val mediaPicker: MediaPicker
) {
    val mediaList: Observable<List<Any>> by mediaPicker::mediaList

    fun loadMedia() {
        mediaPicker.loadVideos()
    }
}