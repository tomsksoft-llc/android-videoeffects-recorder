package com.tomsksoft.videoeffectsrecorder.domain.boundary

import io.reactivex.rxjava3.subjects.BehaviorSubject

interface MediaPicker {
    val mediaList: BehaviorSubject<List<Any>>
    fun loadVideos()
}