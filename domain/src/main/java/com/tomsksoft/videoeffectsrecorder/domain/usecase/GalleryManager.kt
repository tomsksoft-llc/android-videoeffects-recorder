package com.tomsksoft.videoeffectsrecorder.domain.usecase

import android.net.Uri
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class GalleryManager(
    private val mediaPicker: MediaPicker
) {
    val mediaList: Observable<List<Uri>> = mediaPicker.mediaList
        .map{it as List<Uri>}
        .observeOn(Schedulers.io())

    fun loadMedia() {
        mediaPicker.loadVideos()
    }
}