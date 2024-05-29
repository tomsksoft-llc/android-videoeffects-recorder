package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelStub

@Preview
@Composable
fun GalleryWrapperPreview(){
    GalleryWrapper(onGalleryLocalClick = { }, onGalleryAllClick = {}, onCameraClick = {}, viewModel = GalleryViewModelStub, navController = rememberNavController())
}

@Composable
fun GalleryWrapper(
    mainContent: @Composable (ColumnScope.() -> Unit)? = null,
    viewModel: GalleryViewModel,
    navController: NavController,
    onCameraClick: () -> Unit,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit
) {
    // navbackstack entry for retrieving current destination route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedMediaList by viewModel.selectedMediaList.collectAsState()
    val isSelectionOn by viewModel.isSelectionOn.collectAsState()
    var showTrashDialog by remember {mutableStateOf(false)}

    Column {
        GalleryTopBar(
            navBackStackEntry = navBackStackEntry,
            onCameraClick = onCameraClick,
            onQuitSelectionClick = viewModel::toggleSelectionMode,
            isSelectionOn = isSelectionOn,
            onSelectAllClick = viewModel::toggleAllMedia
        )

        // composable for gallery screens are called here
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column {
                mainContent?.invoke(this)
            }
            TrashDialog(
                showTrashDialog = showTrashDialog,
                numberOfItems = selectedMediaList.size,
                onDismissRequest = {showTrashDialog = false},
                onConfirmClicked = viewModel::deleteSelectedMedia
            )
        }

        GalleryBottomBar(
            navBackStackEntry = navBackStackEntry,
            isSelectionOn = isSelectionOn,
            onGalleryLocalClick = onGalleryLocalClick,
            onGalleryAllClick = onGalleryAllClick,
            onDeleteClick = { showTrashDialog = true },
        )
    }
}

@Composable
fun GalleryBottomBar(
    navBackStackEntry: NavBackStackEntry?,
    isSelectionOn: Boolean,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box {
        // default bottom bar with navigation
        GalleryNavigationBar(
            navBackStackEntry = navBackStackEntry,
            onGalleryLocalClick = {
                onGalleryLocalClick()
            },
            onGalleryAllClick = {
                onGalleryAllClick()
            },
            isSelectionOn
        )
        // bottom bar that replaces default navigation bar when selection mode is toggled on
        SelectionGalleryBottomBar(
            isSelectionOn = isSelectionOn,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
fun GalleryTopBar(
    navBackStackEntry: NavBackStackEntry?,
    onCameraClick: () -> Unit,
    onQuitSelectionClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    isSelectionOn: Boolean
) {
    Box {
        // default top bar
        DefaultGalleryTopBar(
            isSelectionOn = isSelectionOn,
            navBackStackEntry = navBackStackEntry,
            onCameraClick = onCameraClick
        )
        //bottom bar that replaces default top bar when selection mode is toggled on
        SelectionGalleryTopBar(
            isSelectionOn = isSelectionOn,
            selectedNumber = 0,
            onBackClick = onQuitSelectionClick,
            onSelectAllClick = onSelectAllClick
        )
    }
}

@Composable
fun GalleryNavigationBar(
    navBackStackEntry: NavBackStackEntry?,
    onGalleryLocalClick: () -> Unit,
    onGalleryAllClick: () -> Unit,
    selectionState: Boolean
) {
    AnimatedVisibility(
        visible = !selectionState,
        enter = slideInVertically{it/2},
        exit = slideOutVertically{it/2}
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
fun DefaultGalleryTopBar(
    isSelectionOn: Boolean,
    navBackStackEntry: NavBackStackEntry?,
    onCameraClick: () -> Unit
) {
    AnimatedVisibility(
        visible = !isSelectionOn,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (navBackStackEntry?.destination?.route == GALLERY_LOCAL_ROUTE)
                        "Effects App media"
                    else
                        "All media on device"
                )
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionGalleryTopBar(
    isSelectionOn: Boolean,
    selectedNumber: Int,
    onBackClick: () -> Unit,
    onSelectAllClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isSelectionOn,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        TopAppBar(
            title = {
                Text(text = if (selectedNumber==0) "Select items" else "$selectedNumber items selected")
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(painter = painterResource(id = R.drawable.ic_clear), contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onSelectAllClick) {
                    Icon(painter = painterResource(id = R.drawable.ic_check_all), contentDescription = null)
                }
            }
        )
    }
}

@Composable
fun SelectionGalleryBottomBar(
    isSelectionOn: Boolean,
    onDeleteClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isSelectionOn,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
            ) {
            Button(onClick = onDeleteClick) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = null
                    )
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Composable
fun TrashDialog(
    showTrashDialog: Boolean,
    numberOfItems: Int,
    onDismissRequest: () -> Unit,
    onConfirmClicked: () -> Unit
) {
    if (showTrashDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                Button(onClick = {
                    onConfirmClicked()
                    onDismissRequest()
                }) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text(text = "Dismiss")
                }
            },
            title = {
                Text(text = "Delete")
            },
            text = {
                Text(text = "Delete $numberOfItems items?")
            }
        )
    }
}