package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import com.tomsksoft.videoeffectsrecorder.domain.usecase.GalleryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModelImpl @Inject constructor(
    private val galleryManager: GalleryManager
): ViewModel(), GalleryViewModel {
    override val mediaList: Observable<List<Media>> = galleryManager.mediaList
    private val _isSelectionOn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _selectedMediaList: MutableStateFlow<List<Uri>> = MutableStateFlow(
        mutableListOf()
    )
    override val isSelectionOn: StateFlow<Boolean> = _isSelectionOn.asStateFlow()
    override val selectedMediaList: StateFlow<List<Uri>> = _selectedMediaList.asStateFlow()

    override fun loadMediaList() {
        viewModelScope.launch {
            galleryManager.loadMedia()
        }
    }

    override fun toggleSelectionMode() {
        _isSelectionOn.update { isSelectionOn ->
            !isSelectionOn
        }
        if (!isSelectionOn.value) _selectedMediaList.update {
            mutableListOf()
        }
    }

    override fun toggleSelectedMedia(uri: Uri) {
        _selectedMediaList.update { selectedMediaList ->
            if (uri in _selectedMediaList.value)
                (selectedMediaList.filter{ it != uri })
            else
                (selectedMediaList + listOf(uri))
        }
    }

    override fun deleteSelectedMedia() {
        viewModelScope.launch {
            galleryManager.deleteMedia(selectedMediaList.value.map { it.toString() })
        }
    }

    override fun toggleAllMedia() {
        _selectedMediaList.update {
            if (selectedMediaList.value.size != mediaList.blockingFirst().size)
                mediaList.blockingFirst().map {
                    it.uri.toUri()
                }
            else
                listOf()
        }
    }
}