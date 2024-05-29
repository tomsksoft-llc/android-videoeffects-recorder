package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.StateFlow

interface GalleryViewModel {
    val mediaList: Observable<List<Media>>
    val isSelectionOn: StateFlow<Boolean>
    val selectedMediaList: StateFlow<List<Uri>>
    fun loadMediaList()
    fun toggleSelectionMode()
    fun toggleSelectedMedia(uri: Uri)
    fun deleteSelectedMedia()
    fun toggleAllMedia()
}