package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.tomsksoft.videoeffectsrecorder.R
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
    LaunchedEffect(Unit) {
        viewModel.loadMediaList()
    }
    val isSelectionOn: Boolean by viewModel.isSelectionOn.collectAsState()
    val mediaList: List<Uri> by viewModel.mediaList.subscribeAsState(initial = listOf())
    val selectedMediaList: List<Uri> by viewModel.selectedMediaList.collectAsState()

    BackHandler {
        if (isSelectionOn) viewModel.toggleSelectionMode()
        else onCameraClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        MediaGrid(
            mediaList = mediaList,
            isSelectionOn = isSelectionOn,
            selectedMediaList = selectedMediaList,
            onMediaItemLongClick = {
                if (!isSelectionOn) viewModel.toggleSelectionMode()
                viewModel.toggleSelectedMedia(it)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    mediaList: List<Uri>,
    selectedMediaList: List<Uri>,
    isSelectionOn: Boolean,
    onMediaItemLongClick: (Uri) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp),
        contentPadding = PaddingValues(2.dp)
    ) {
        Log.d("MediaGrid", "number of videos ${mediaList.count()}")
        items(
            mediaList
        ) {mediaItem ->
            val isMediaSelected = mediaItem in selectedMediaList
            if (isSelectionOn) {
                Box(
                    modifier = Modifier
                        .scale(if (isMediaSelected) 0.9f else 1f)
                        .clickable { onMediaItemLongClick(mediaItem) }
                ) {
                    MediaImage(
                        uri = mediaItem
                    )
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Checkbox(checked = isMediaSelected, onCheckedChange = {onMediaItemLongClick(mediaItem)})
                    }
                }
            } 
            else {
                Box(
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {onMediaItemLongClick(mediaItem)}
                    )
                ) {
                    MediaImage(
                        uri = mediaItem
                    )
                }
            }
        }
    }
}

@Composable
fun MediaImage(
    uri: Uri
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
    ) {
        if (uri.toString().contains("video")) {
            val model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .decoderFactory{result, options, _ ->
                    VideoFrameDecoder(
                        result.source,
                        options
                    )
                }
                .build()
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Icon(
                modifier = Modifier
                    .shadow(1.dp, CircleShape),
                painter = painterResource(id = R.drawable.ic_video_media),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background
            )
        }
        else if (uri.toString().contains("image")) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}