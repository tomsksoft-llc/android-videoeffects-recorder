package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.core.Observable

interface FrameProvider<F: Any> {
    val frame: Observable<F>
}