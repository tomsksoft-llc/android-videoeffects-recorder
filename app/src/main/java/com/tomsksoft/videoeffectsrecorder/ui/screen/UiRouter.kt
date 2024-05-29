package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
					CameraScreen{navController.navigate(GALLERY_ROUTE)}
				}
				composable(route = GALLERY_ROUTE) {
					val galleryNavController = rememberNavController()
					val viewModel = hiltViewModel<GalleryViewModelImpl>()
					GalleryWrapper(
						viewModel = viewModel,
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
                                        viewModel = viewModel,
										onMediaClick = { id ->
											navController.navigate("MediaScreen" + "?id=${id}")
										},
										onCameraClick = { navController.navigate(CAMERA_ROUTE) }
									)
								}
								composable(route = GALLERY_ALL_ROUTE) {
									GalleryScreenAll(viewModel)
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