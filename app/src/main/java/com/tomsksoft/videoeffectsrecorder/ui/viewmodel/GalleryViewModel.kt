package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.net.Uri
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.StateFlow

interface GalleryViewModel {
    val mediaList: Observable<List<Uri>>
}