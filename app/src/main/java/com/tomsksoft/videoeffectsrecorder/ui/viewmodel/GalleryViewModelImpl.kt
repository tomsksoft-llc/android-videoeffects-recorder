package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.domain.usecase.GalleryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModelImpl @Inject constructor(
    private val galleryManager: GalleryManager
): ViewModel(), GalleryViewModel {
    override val mediaList: Observable<List<Uri>> = galleryManager.mediaList
        .map { it.map(Uri::parse) }

    override fun loadMediaList() {
        viewModelScope.launch {
            galleryManager.loadMedia()
        }
    }
}