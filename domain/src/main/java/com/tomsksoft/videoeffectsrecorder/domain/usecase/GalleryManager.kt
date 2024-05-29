package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class GalleryManager(
    private val mediaPicker: MediaPicker
) {
    private val _mediaList: BehaviorSubject<List<Media>> = BehaviorSubject.create()
    val mediaList: Observable<List<Media>>
        get() = _mediaList

    fun loadMedia() {
        _mediaList.onNext(mediaPicker.loadMedia())
    }

    fun deleteMedia(uriList: List<String>) {
        mediaPicker.deleteVideos(uriList)
        loadMedia()
    }
}