package com.tomsksoft.videoeffectsrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tomsksoft.videoeffectsrecorder.ui.screen.UiRouter
import com.tomsksoft.videoeffectsrecorder.ui.theme.VideoEffectsRecorderTheme
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {
	companion object {
		val PERMISSIONS = mutableListOf(
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO
		).also { permissions ->
			// up to Android 12 inclusive (32 API)
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
				permissions.addAll(listOf(
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.READ_EXTERNAL_STORAGE
				))
		}.toTypedArray()
	}

	private val viewModel by viewModels<CameraViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		obtainPermissions { viewModel.initializeCamera(this) }
		setContent {
			VideoEffectsRecorderTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					UiRouter.VideoEffectsRecorderApp()
				}
			}
		}
	}

	private fun obtainPermissions(onGranted: () -> Unit) {
		if (PERMISSIONS.any {
			ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
		})
			registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
				if (it.values.any { granted -> !granted }) { // any permission denied
					Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
					finish()
				}
				onGranted() // just granted
			}.launch(PERMISSIONS)
		else onGranted() // there already are permissions
	}
}

