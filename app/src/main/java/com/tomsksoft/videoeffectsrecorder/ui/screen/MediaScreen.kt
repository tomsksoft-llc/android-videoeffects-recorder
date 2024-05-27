package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Preview
@Composable
private fun MediaScreenPreview() {
    MediaScreen(GalleryViewModelStub,null,{},{})
}

@Composable
fun MediaScreen(
    viewModel: GalleryViewModel,
    id: Long?,
    onGalleryClick: () -> Unit,
    onEditMedia: (String) -> Unit
) {
    BackHandler {
        onGalleryClick()
    }
    val mediaList by viewModel.mediaList.subscribeAsState(initial = listOf())
    val currentMedia = mediaList.find { media ->
        media.id == id
    }

    if (currentMedia != null) {
        Box (
            modifier = Modifier
                .background(Color.Red)
                .fillMaxSize()
        ) {
            Log.d("MediaScreen", currentMedia.uri)
            if (currentMedia.isVideo) {
                val model = ImageRequest.Builder(LocalContext.current)
                    .data(currentMedia.uri)
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
            } else {
                AsyncImage(
                    model = currentMedia.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            MediaViewBottomBar(
                currentMedia = currentMedia,
                showUI = true,
                onEditMedia = {onEditMedia(currentMedia.uri)}
            )
        }
    }
}

@Composable
fun BoxScope.MediaViewBottomBar(
    currentMedia: Media,
    showUI: Boolean,
    onEditMedia: () -> Unit
) {
    AnimatedVisibility(
        visible = showUI,
        enter = slideInVertically{it/2},
        exit = slideOutVertically {it/2},
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .padding(
                    top = 24.dp,
                    bottom = 12.dp
                )
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            currentMedia?.let {
                MediaViewActions(
                    currentMedia = it,
                    onEditMedia = onEditMedia,
                )
            }
        }
    }
    /*currentMedia?.let {
        MediaInfoBottomSheet(
            media = it,
            state = bottomSheetState,
            albumsState = albumsState,
            handler = handler
        )
    }*/
}

@Composable
private fun MediaViewActions(
    //currentIndex: Int,
    currentMedia: Media,
    //handler: MediaHandleUseCase,
    onEditMedia: () -> Unit,
    //showDeleteButton: Boolean
) {
    // Edit
    IconButton(onClick = onEditMedia) {
        Column {
            Icon(painter = painterResource(id = R.drawable.ic_filter_preset), contentDescription = null)
        }
    }
}