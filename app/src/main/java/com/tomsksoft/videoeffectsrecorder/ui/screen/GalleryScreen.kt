package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelImpl
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.GalleryViewModelStub

private enum class Route(
    val screenTitle: String,
    val menuItemTitle: String,
    @DrawableRes val icon: Int
) {
    // TODO [fmv] add appropriate icon
    LOCAL("Effects App media", "Effects SDK", R.drawable.ic_photo),
    GLOBAL("All media on device", "Global Storage", R.drawable.ic_more);
}

@Preview
@Composable
fun GalleryScreenPreview() =
    GalleryScreen(viewModel = GalleryViewModelStub)

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel<GalleryViewModelImpl>(),
    navController: NavHostController = rememberNavController(),
    onCameraClick: () -> Unit = {}
) {
    // navbackstack entry for retrieving current destination route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedMediaList by viewModel.selectedMediaList.collectAsState()
    val isSelectionOn by viewModel.isSelectionOn.collectAsState()
    var showTrashDialog by remember { mutableStateOf(false) }

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
            NavHost(
                navController = navController,
                startDestination = Route.LOCAL.toString()
            ) {
                composable(route = Route.LOCAL.toString()) {
                    LocalMediaRoute(
                        viewModel = viewModel,
                        onCameraClick = onCameraClick
                    )
                }
                composable(route = Route.GLOBAL.toString()) {
                    GlobalMediaRoute(viewModel)
                }
            }
            TrashDialog(
                showTrashDialog = showTrashDialog,
                numberOfItems = selectedMediaList.size,
                onDismissRequest = { showTrashDialog = false },
                onConfirmClicked = viewModel::deleteSelectedMedia
            )
        }

        GalleryBottomBar(
            navBackStackEntry = navBackStackEntry,
            isSelectionOn = isSelectionOn,
            onMenuItemClick = { route -> navController.navigate(route.toString()) },
            onDeleteClick = { showTrashDialog = true },
        )
    }
}

/* Routes */

@Composable
private fun GlobalMediaRoute(
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

@Composable
private fun LocalMediaRoute(
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

/* Screen Components */

@Composable
private fun GalleryBottomBar(
    navBackStackEntry: NavBackStackEntry?,
    isSelectionOn: Boolean,
    onMenuItemClick: (Route) -> Unit,
    onDeleteClick: () -> Unit
) {
    Box {
        // default bottom bar with navigation
        GalleryNavigationBar(
            navBackStackEntry = navBackStackEntry,
            onMenuItemClick = onMenuItemClick,
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
private fun GalleryNavigationBar(
    navBackStackEntry: NavBackStackEntry?,
    onMenuItemClick: (Route) -> Unit,
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
            val currentRoute: Route? = navBackStackEntry?.destination?.route?.let(Route::valueOf)
            for (route in Route.values())
                NavigationBarItem(
                    selected = currentRoute == route,
                    onClick = { onMenuItemClick(route) },
                    icon = {
                        NavigationBarIcon(
                            icon = route.icon,
                            text = route.menuItemTitle
                        )
                    }
                )
        }
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
                navBackStackEntry?.destination?.route?.let {
                    Text(Route.valueOf(it).screenTitle)
                }
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

/* Views */

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