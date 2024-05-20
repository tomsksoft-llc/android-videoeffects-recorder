package com.tomsksoft.videoeffectsrecorder.domain.boundary

interface MediaPicker {
    /**
     * @return URI's
     */
    fun loadVideos(): List<String>
}