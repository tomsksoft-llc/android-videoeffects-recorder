package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.entity.GalleryRoute

@Preview
@Composable
fun GalleryWrapperPreview(){
    GalleryWrapper(onGalleryLocalClick = { }, onGalleryAllClick = {}, onCameraClick = {})
}

@Composable
fun GalleryWrapper(
    mainContent: @Composable (ColumnScope.() -> Unit)? = null,
    onCameraClick: () -> Unit,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    var screenState by remember { mutableStateOf(GalleryRoute.LOCAL) }
    Column {
        GalleryTopBar(
            screenState,
            onCameraClick
        )
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            mainContent?.invoke(this)
        }
        GalleryNavigationBar(
            screenState = screenState,
            onGalleryLocalClick = {
                onGalleryLocalClick()
                screenState = GalleryRoute.LOCAL
            },
            onGalleryAllClick = {
                onGalleryAllClick()
                screenState = GalleryRoute.ALL
            }
        )
    }
}

@Composable
fun GalleryNavigationBar(
    screenState: GalleryRoute,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    NavigationBar (
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = screenState == GalleryRoute.LOCAL,
            onClick = onGalleryLocalClick,
            icon = {
                NavigationBarIcon(
                    icon = R.drawable.ic_photo, // TODO [fmv] add appropriate icon
                    text = "Effects SDK"
                )
            }
        )
        NavigationBarItem(
            selected = screenState == GalleryRoute.ALL,
            onClick = onGalleryAllClick,
            icon = {
                NavigationBarIcon(
                    icon = R.drawable.ic_more, // TODO [fmv] add appropriate icon
                    text = "Global storage",
                )
            }
        )
    }
}

@Composable
fun NavigationBarIcon(
    @DrawableRes icon: Int,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color
        )
        Text(
            text = text,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    screenState: GalleryRoute,
    onCameraClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = if (screenState == GalleryRoute.LOCAL) "Effects App media" else "All media on device")
        },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            IconButton(onClick = onCameraClick) {
                Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = null)
            }             
        }
    )
}