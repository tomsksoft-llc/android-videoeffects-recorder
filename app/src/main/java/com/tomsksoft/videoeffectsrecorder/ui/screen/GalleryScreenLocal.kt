package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
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
import com.tomsksoft.videoeffectsrecorder.domain.entity.Media
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelStub

@Composable
@Preview
fun GalleryScreenLocalPreview() {
    GalleryScreenLocal(GalleryViewModelStub, {}, {})
}

@Composable
fun GalleryScreenLocal(
    viewModel: GalleryViewModel,
    onCameraClick: () -> Unit,
    onMediaClick: (id: Long) -> Unit
) {
    BackHandler {
        onCameraClick()
    }
    LaunchedEffect(Unit) {
        viewModel.loadMediaList()
    }
    val mediaList: List<Media> by viewModel.mediaList.subscribeAsState(initial = listOf())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        MediaGrid(
            mediaList = mediaList,
            onMediaClick = onMediaClick
        )
    }
}

@Composable
fun MediaGrid(
    mediaList: List<Media>,
    onMediaClick: (id: Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp),
        contentPadding = PaddingValues(2.dp)
    ) {
        Log.d("MediaGrid", "number of videos ${mediaList.count()}")
        items(
            mediaList
        ) {mediaItem ->
            MediaImage(
                media = mediaItem,
                onMediaClick = onMediaClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaImage(
    media: Media,
    onMediaClick: (id: Long) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = {onMediaClick(media.id)}
            )
    ) {
        if (media.isVideo) {
            val model = ImageRequest.Builder(LocalContext.current)
                .data(media.uri)
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
        else {
            AsyncImage(
                model = media.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}