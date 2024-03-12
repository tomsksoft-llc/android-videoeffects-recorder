package com.tomsksoft.videoeffectsrecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tomsksoft.videoeffectsrecorder.ui.screen.UiRouter
import com.tomsksoft.videoeffectsrecorder.ui.theme.VideoEffectsRecorderTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			VideoEffectsRecorderTheme(darkTheme = false) {
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
}