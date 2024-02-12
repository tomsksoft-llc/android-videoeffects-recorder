package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel

object UiRouter {
	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun VideoEffectsRecorderApp(
		navController: NavHostController = rememberNavController()
	) {
		Scaffold { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = "CameraScreen",
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				composable(route = "CameraScreen") {
					CameraScreen()
				}
			}
		}
	}
}