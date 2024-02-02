package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object UiRouter {


	// TODO: [avp] Place all navigation here

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun VideoEffectsRecorderApp(
		navController: NavHostController = rememberNavController()
	) {
		Scaffold { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = "SplashScreen",
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(innerPadding)
			) {
				composable(route = "SplashScreen") {
					Log.d("TAG_GG", "TestScreen")
					SplashScreen()
				}
			}
		}
	}
}