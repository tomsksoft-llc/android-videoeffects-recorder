package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import io.reactivex.rxjava3.core.Observable

interface GalleryViewModel {
    val mediaList: Observable<List<Media>>
    fun loadMediaList()
}