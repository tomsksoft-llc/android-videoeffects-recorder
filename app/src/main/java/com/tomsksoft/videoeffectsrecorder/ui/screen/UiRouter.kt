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

/* Routes */
const val CAMERA_ROUTE = "CameraScreen"
const val GALLERY_ROUTE = "GalleryScreen"
const val GALLERY_LOCAL_ROUTE = "GalleryLocalScreen"
const val GALLERY_ALL_ROUTE = "GalleryAllScreen"
/* * */

object UiRouter {
	@Composable
	fun VideoEffectsRecorderApp(
		navController: NavHostController = rememberNavController()
	) {
		Scaffold { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = CAMERA_ROUTE,
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				composable(route = CAMERA_ROUTE) {
					CameraScreen({navController.navigate(GALLERY_ROUTE)})
				}
				composable(route = GALLERY_ROUTE) {
					val galleryNavController = rememberNavController()
					GalleryWrapper(
						navController = galleryNavController,
						onGalleryLocalClick = { galleryNavController.navigate(GALLERY_LOCAL_ROUTE) },
						onGalleryAllClick =  { galleryNavController.navigate(GALLERY_ALL_ROUTE) },
						onCameraClick = {navController.navigate(CAMERA_ROUTE)},
						mainContent = {
							NavHost(
								navController = galleryNavController,
								startDestination = GALLERY_LOCAL_ROUTE,
								route = GALLERY_ROUTE
							) {
								composable(route = GALLERY_LOCAL_ROUTE) {
									GalleryScreenLocal(
										viewModel = hiltViewModel<GalleryViewModelImpl>(),
										onCameraClick = { navController.navigate(CAMERA_ROUTE) }
									)
								}
								composable(route = GALLERY_ALL_ROUTE) {
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