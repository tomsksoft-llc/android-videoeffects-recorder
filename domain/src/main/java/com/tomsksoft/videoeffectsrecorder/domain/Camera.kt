package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.core.Observable

interface Camera {
    val frameSource: Observable<Any>
    val orientation: Int
    var isEnabled: Boolean
    var direction: Direction

    enum class Direction {
        FRONT,
        BACK
    }
}