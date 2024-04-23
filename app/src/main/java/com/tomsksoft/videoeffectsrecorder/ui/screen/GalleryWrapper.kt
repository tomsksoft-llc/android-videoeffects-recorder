package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.tomsksoft.videoeffectsrecorder.R

@Preview
@Composable
fun GalleryWrapperPreview(){
    GalleryWrapper(onGalleryLocalClick = { }, onGalleryAllClick = {})
}

@Composable
fun GalleryWrapper(
    mainContent: @Composable (ColumnScope.() -> Unit)? = null,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    Column {
        GalleryTopBar()
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            mainContent?.invoke(this)
        }
        GalleryNavigationBar(
            onGalleryLocalClick = onGalleryLocalClick,
            onGalleryAllClick = onGalleryAllClick
        )
    }
}

@Composable
fun GalleryNavigationBar(
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    NavigationBar (
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onGalleryLocalClick,
            icon = {
                NavigationBarIcon(
                    icon = R.drawable.ic_photo,
                    text = "Effects SDK"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onGalleryAllClick,
            icon = {
                NavigationBarIcon(
                    icon = R.drawable.ic_more,
                    text = "Global storage"
                )
            }
        )
    }
}

@Composable
fun NavigationBarIcon(
    @DrawableRes icon: Int,
    text: String,
    color: Color = MaterialTheme.colorScheme.background
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon), // TODO [fmv] add appropriate icon
            contentDescription = null,
            tint = color
        )
        Text(
            text = text,
            color = color
        )
    }
}

@Composable
fun GalleryTopBar(  // TODO [fmv] add back arrow for navigation back to camera screen and other content

) {

}