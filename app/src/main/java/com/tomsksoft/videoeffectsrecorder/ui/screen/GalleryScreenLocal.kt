package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.media.effect.Effect
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelStub

@Composable
@Preview
fun GalleryScreenLocalPreview() {
    GalleryScreenLocal(GalleryViewModelStub, {})
}

@Composable
fun GalleryScreenLocal(
    viewModel: GalleryViewModel,
    onCameraClick: () -> Unit
) {
    BackHandler {
        onCameraClick()
    }
    LaunchedEffect(Unit) {
        viewModel.loadMediaList()
    }
    val mediaList: List<Uri> by viewModel.mediaList.subscribeAsState(initial = listOf())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.onBackground) // TODO [fmv] change to ".background" when theme issues will be fixed
    ) {
        MediaGrid(mediaList = mediaList)
    }
}

@Composable
fun MediaGrid(
    mediaList: List<Uri>
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp)
    ) {
        Log.d("MediaGrid", "number of videos ${mediaList.count()}")
        items(
            mediaList
        ) {mediaItem ->
            MediaImage(uri = mediaItem)
        }
    }
}

@Composable
fun MediaImage(
    uri: Uri
) {

}