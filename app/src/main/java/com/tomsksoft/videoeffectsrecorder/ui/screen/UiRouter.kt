package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.EditMediaViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.EditMediaViewModelImpl
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelImpl

object UiRouter {
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
						onGalleryAllClick =  { galleryNavController.navigate("GalleryAllScreen") },
						onCameraClick = {navController.navigate("CameraScreen")},
						mainContent = {
							NavHost(
								navController = galleryNavController,
								startDestination = "GalleryLocalScreen",
								route = "GalleryScreen"
							) {
								composable(route = "GalleryLocalScreen") {
									GalleryScreenLocal(
										viewModel = hiltViewModel<GalleryViewModelImpl>(),
										onCameraClick = { navController.navigate("CameraScreen") },
										onMediaClick = { id ->
											navController.navigate("MediaScreen" + "?id=${id}")
										}
									)
								}
								composable(route = "GalleryAllScreen") {
									GalleryScreenAll(hiltViewModel<GalleryViewModelImpl>())
								}
							}
						}
					)
				}
				composable(
					route = "MediaScreen?id={id}",
					arguments = listOf(
						navArgument(name = "id") {
							type = NavType.LongType
						}
					)
				) {navBackStackEntry ->
					val id = navBackStackEntry.arguments?.getLong("id")
					MediaScreen(
						viewModel = hiltViewModel<GalleryViewModelImpl>(),
						id = id,
						onGalleryClick = { navController.navigate("GalleryScreen") },
						onEditMedia = {uri ->
							navController.navigate("EditMediaScreen" + "?uri=${uri}")
						}
					)
				}
				composable(
					route = "EditMediaScreen?uri={uri}",
					arguments = listOf(
						navArgument(name = "uri") {
							type = NavType.StringType
						}
					)
				) { navBackStackEntry ->
					val uri = navBackStackEntry.arguments?.getString("uri")
					EditMediaScreen(
						viewModel = hiltViewModel<EditMediaViewModelImpl>(),
						uri = uri
					)
				}
			}
		}
	}
}