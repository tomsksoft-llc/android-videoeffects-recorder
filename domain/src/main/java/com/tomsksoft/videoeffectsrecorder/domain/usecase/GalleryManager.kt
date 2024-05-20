package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class GalleryManager(
    private val mediaPicker: MediaPicker
) {
    private val _mediaList: BehaviorSubject<List<String>> = BehaviorSubject.create()
    val mediaList: Observable<List<String>>
        get() = _mediaList

    fun loadMedia() {
        _mediaList.onNext(mediaPicker.loadVideos())
    }
}