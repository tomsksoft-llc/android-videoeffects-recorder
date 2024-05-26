package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object GalleryViewModelStub: GalleryViewModel {
    override val mediaList: Observable<List<Uri>> = BehaviorSubject.create()
    override val isSelectionOn: StateFlow<Boolean> = MutableStateFlow(true)
    override val selectedMediaList: StateFlow<MutableList<Uri>> = MutableStateFlow(mutableListOf())

    override fun loadMediaList() = throw unimplementedError()
    override fun toggleSelectionMode() = throw unimplementedError()
    override fun toggleSelectedMedia(uri: Uri) = throw unimplementedError()
    override fun deleteSelectedMedia() = throw unimplementedError()
    override fun toggleAllMedia() = throw unimplementedError()

    private fun unimplementedError() = NotImplementedError("Stub doesn't implement any logic") //maybe move this out somewhere
}