package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryScreenAll(
    viewModel: GalleryViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.onBackground) // TODO [fmv] change to ".background" when theme issues will be fixed
    ) {
        // TODO [fmv] add representation for files not created by the app
    }
}