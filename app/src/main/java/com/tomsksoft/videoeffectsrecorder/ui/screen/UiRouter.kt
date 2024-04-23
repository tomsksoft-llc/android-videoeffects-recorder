package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelImpl

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
					CameraScreen({navController.navigate("GalleryScreen")})
				}
				composable(route = "GalleryScreen") {
					val galleryNavController = rememberNavController()
					GalleryWrapper(
						onGalleryLocalClick = { galleryNavController.navigate("GalleryLocalScreen") },
						onGalleryAllClick =  { galleryNavController.navigate("GalleryAllClick") },
						mainContent = {
							NavHost(
								navController = galleryNavController,
								startDestination = "GalleryLocalScreen",
								route = "GalleryScreen"
							) {
								composable(route = "GalleryLocalScreen") {
									GalleryScreenLocal(
										viewModel = hiltViewModel<GalleryViewModelImpl>(),
										onCameraClick = { navController.navigate("CameraScreen") }
									)
								}
								composable(route = "GalleryAllClick") {
									GalleryScreenAll(hiltViewModel<GalleryViewModelImpl>())
								}
							}
						}
					)
				}
			}
		}
	}
}