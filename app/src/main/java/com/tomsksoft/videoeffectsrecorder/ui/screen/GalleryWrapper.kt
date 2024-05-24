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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tomsksoft.videoeffectsrecorder.R

@Preview
@Composable
fun GalleryWrapperPreview(){
    GalleryWrapper(onGalleryLocalClick = { }, onGalleryAllClick = {}, onCameraClick = {}, navController = rememberNavController())
}

@Composable
fun GalleryWrapper(
    mainContent: @Composable (ColumnScope.() -> Unit)? = null,
    navController: NavController,
    onCameraClick: () -> Unit,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    Column {
        GalleryTopBar(
            navBackStackEntry,
            onCameraClick
        )
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            mainContent?.invoke(this)
        }
        GalleryNavigationBar(
            navBackStackEntry = navBackStackEntry,
            onGalleryLocalClick = {
                onGalleryLocalClick()
            },
            onGalleryAllClick = {
                onGalleryAllClick()
            }
        )
    }
}

@Composable
fun GalleryNavigationBar(
    navBackStackEntry: NavBackStackEntry?,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    NavigationBar (
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val currentRoute = navBackStackEntry?.destination?.route
        NavigationBarItem(
            selected = currentRoute == GALLERY_LOCAL_ROUTE,
            onClick = onGalleryLocalClick,
            icon = {
                NavigationBarIcon(
                    icon = R.drawable.ic_photo, // TODO [fmv] add appropriate icon
                    text = "Effects SDK"
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == GALLERY_ALL_ROUTE,
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
    navBackStackEntry: NavBackStackEntry?,
    onCameraClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = if (navBackStackEntry?.destination?.route == GALLERY_LOCAL_ROUTE) "Effects App media" else "All media on device")
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