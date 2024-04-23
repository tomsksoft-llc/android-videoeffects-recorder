package com.tomsksoft.videoeffectsrecorder.data

import android.net.Uri
import com.tomsksoft.videoeffectsrecorder.domain.MediaPicker
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MediaPickerImpl: MediaPicker {
    override val mediaList: BehaviorSubject<List<Any>> = BehaviorSubject.create()

    fun loadVideos() {
        val uriList = listOf<Uri>()
        mediaList.onNext(uriList)
    }
}