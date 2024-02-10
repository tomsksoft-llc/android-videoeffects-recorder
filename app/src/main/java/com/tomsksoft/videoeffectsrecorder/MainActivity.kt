package com.tomsksoft.videoeffectsrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tomsksoft.videoeffectsrecorder.ui.screen.UiRouter
import com.tomsksoft.videoeffectsrecorder.ui.theme.VideoEffectsRecorderTheme
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<CameraViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		obtainCameraPermission()
		viewModel.initializeCamera(this)
		setContent {
			VideoEffectsRecorderTheme {
				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					UiRouter.VideoEffectsRecorderApp()
				}
			}
		}
	}

	private fun obtainCameraPermission() {
		if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
			requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
	}
}

