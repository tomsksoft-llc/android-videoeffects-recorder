package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

object GalleryViewModelStub: GalleryViewModel {
    override val mediaList: Observable<List<Media>> = BehaviorSubject.create()
    override fun loadMediaList() = throw unimplementedError()

    private fun unimplementedError() = NotImplementedError("Stub doesn't implement any logic") //maybe move this out somewhere
}