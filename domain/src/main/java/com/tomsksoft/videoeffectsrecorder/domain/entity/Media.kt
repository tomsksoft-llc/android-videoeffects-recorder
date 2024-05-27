package com.tomsksoft.videoeffectsrecorder.domain.entity

data class Media(
    val id: Long,
    val uri: String,
    val label: String,
    val isVideo: Boolean
)
