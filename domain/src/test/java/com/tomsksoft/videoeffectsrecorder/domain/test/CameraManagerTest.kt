package com.tomsksoft.videoeffectsrecorder.domain.test

import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import com.tomsksoft.videoeffectsrecorder.domain.mock.CameraMock
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraManager
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CameraManagerTest {
	companion object {
		const val FIRST_FRAME_TIMEOUT = 5000 // ms
	}

	private val camera = CameraMock()
	private val manager = CameraManager(camera)

	@Test
	fun testProperties() {
		manager.setSurface(null) // it's actually stub
		val config = CameraConfig()
		manager.cameraConfig.onNext(config)
		assertEquals(config, camera.cameraConfig) // check if manager pass by config
		manager.direction = Camera.Direction.BACK
		assertEquals(Camera.Direction.BACK, manager.direction)
		manager.direction = Camera.Direction.FRONT
		assertEquals(Camera.Direction.FRONT, manager.direction)
		assert(manager.orientation in 0..359)
	}

	@Test
	fun testFlash() {
		manager.apply {
			isEnabled = true

			flashMode = FlashMode.ON
			assertEquals(true, isFlashEnabled)
			isFlashEnabled = false // this attempt doesn't take effect unless AUTO is chosen
			assertEquals(true, isFlashEnabled)
			assertEquals(FlashMode.ON, flashMode)

			flashMode = FlashMode.OFF
			assertEquals(false, isFlashEnabled)
			isFlashEnabled = true
			assertEquals(false, isFlashEnabled)
			assertEquals(FlashMode.OFF, flashMode)

			flashMode = FlashMode.AUTO
			assertEquals(false, isFlashEnabled) // flash should be disabled in this mode by default
			isFlashEnabled = true
			assertEquals(true, isFlashEnabled)
			isFlashEnabled = false
			assertEquals(false, isFlashEnabled)
			assertEquals(FlashMode.AUTO, flashMode)

			isEnabled = false
		}
	}

	@Test
	fun testFramesEmitting() {
		manager.apply {
			isEnabled = true

			var gotFrame = false
			val disposable = frameSource.subscribeOn(Schedulers.io()).subscribe { gotFrame = true }
			val skipAt = System.currentTimeMillis() + FIRST_FRAME_TIMEOUT

			while (!gotFrame && System.currentTimeMillis() < skipAt) Thread.sleep(50)
			disposable.dispose()

			isEnabled = false

			assert(gotFrame) { "Didn't get any frame for $FIRST_FRAME_TIMEOUT ms" }
		}
	}
}