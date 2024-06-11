package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelImpl

private const val ROUTE_CAMERA = "CameraScreen"
private const val ROUTE_GALLERY = "GalleryScreen"

object UiRouter {
	@Composable
	fun VideoEffectsRecorderApp(
		navController: NavHostController = rememberNavController()
	) {
		Scaffold { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = ROUTE_CAMERA,
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				composable(route = ROUTE_CAMERA) {
					CameraScreen(onGalleryClick = { navController.navigate(ROUTE_GALLERY) })
				}
				composable(route = ROUTE_GALLERY) {
					GalleryScreen(onCameraClick = { navController.navigate(ROUTE_CAMERA) })
				}
			}
		}
	}
}