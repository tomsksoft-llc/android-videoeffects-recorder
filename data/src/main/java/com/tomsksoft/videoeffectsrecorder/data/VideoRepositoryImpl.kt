package com.tomsksoft.videoeffectsrecorder.data

import com.tomsksoft.videoeffectsrecorder.domain.VideoRepository

class VideoRepositoryImpl: VideoRepository<Frame> {
    override fun onFrame(frame: Frame) {
        TODO("[tva] save video")
    }

    override fun startRecord(): VideoRepository.Record<Frame> {
        TODO("[tva] record video")
    }
}