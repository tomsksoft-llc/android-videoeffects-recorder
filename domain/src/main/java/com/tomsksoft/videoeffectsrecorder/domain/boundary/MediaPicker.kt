package com.tomsksoft.videoeffectsrecorder.domain.boundary

import com.tomsksoft.videoeffectsrecorder.domain.entity.Media

interface MediaPicker {
    /**
     * @return URI's
     */
    fun loadMedia(): List<Media>
}