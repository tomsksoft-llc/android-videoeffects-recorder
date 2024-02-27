package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.core.Observable

interface FrameProvider {
    val frame: Observable<Any>
    val degree: Observable<Int>
}