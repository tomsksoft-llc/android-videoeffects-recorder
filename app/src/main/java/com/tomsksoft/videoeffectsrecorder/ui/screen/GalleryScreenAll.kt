package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryScreen
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryScreenAll(
    viewModel: GalleryViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // TODO [fmv] add representation for files not created by the app
    }
}