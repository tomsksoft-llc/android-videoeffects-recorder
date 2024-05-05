package com.tomsksoft.videoeffectsrecorder.domain.test

import com.tomsksoft.videoeffectsrecorder.domain.mock.CameraMock
import com.tomsksoft.videoeffectsrecorder.domain.mock.PhotoPickerMock
import com.tomsksoft.videoeffectsrecorder.domain.mock.VideoRecorderMock
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

class CameraRecordManagerTest {

    private val recorder = VideoRecorderMock()
    private val manager = CameraRecordManager(
        CameraManager(CameraMock()),
        recorder,
        PhotoPickerMock() // blocks for 1 second
    )

    @Test
    @Timeout(1, unit = TimeUnit.SECONDS)
    fun testPhotographing() = manager.takePhoto() // manager MUST take photo in async manner

    @Test
    fun testRecordingVideo() {
        manager.isRecording = true
        assertEquals(true, recorder.isRecording)
        manager.isRecording = false
        assertEquals(false, recorder.isRecording)
    }
}