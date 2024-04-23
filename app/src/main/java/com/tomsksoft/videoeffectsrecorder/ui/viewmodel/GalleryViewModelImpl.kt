package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tomsksoft.videoeffectsrecorder.domain.GalleryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GalleryViewModelImpl @Inject constructor(
    private val galleryManager: GalleryManager
): ViewModel(), GalleryViewModel {
    override val mediaList: Observable<List<Uri>> = galleryManager.mediaList
}