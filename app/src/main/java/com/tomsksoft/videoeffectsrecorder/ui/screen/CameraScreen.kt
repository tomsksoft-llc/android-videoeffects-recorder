package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.VideoEffectsRecorderApplication
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ExpandedTopBarMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.FiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.FlashMode
import kotlinx.coroutines.flow.distinctUntilChanged

// TODO [tva] preview has broken because of ComponentActivity reference
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
	val activity = LocalContext.current as ComponentActivity
	val viewModel = viewModel<CameraViewModel>()
	val frame by viewModel.frame.subscribeAsState(null)
	val cameraUiState: CameraUiState by viewModel.cameraUiState.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
		if (uri != null)
			viewModel.setBackground(
				activity.contentResolver.openInputStream(uri)!!
			) // TODO [tva] check on Android 9 or below
	}
	val permissionsLauncher = rememberMultiplePermissionsState(
		permissions = mutableListOf(
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO
		).also { permissions ->
			// direct access to file system up to 9 Android; since 10 MediaStore is used
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
				permissions.addAll(listOf(
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.READ_EXTERNAL_STORAGE
				))
		}
	) {
		if (it.values.any { granted -> !granted }) { // any permission denied
			Toast.makeText(activity, "No permission", Toast.LENGTH_SHORT).show()
			activity.finish()
		}
		viewModel.initializeCamera(lifecycleOwner = activity, activity) // all are granted
	}
	LaunchedEffect(Unit) { permissionsLauncher.launchMultiplePermissionRequest() }

	// keep screen on
	DisposableEffect(Unit) {
		val window = activity.window
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
	}

	if(!cameraUiState.isCameraInitialized){
		Column(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			CircularProgressIndicator(modifier = Modifier.size(50.dp))
			Text(text = stringResource(R.string.camera_not_ready), color = Color.White)
		}
	}
	else {
		Box {
			// effects sdk camera feed; stays behind all other elements
			EffectsCameraPreview(frame, snackbarHostState)

			// elements of ui on top of the camera feed
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.fillMaxWidth(),
				verticalArrangement = Arrangement.SpaceBetween,
			) {

				TopBar(
					cameraUiState,
					viewModel::toggleQuickSettingsIndicator,
					viewModel::setFlash,
					viewModel::setFilters,
				)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					CameraSnackbar(
						snackbarHostState = snackbarHostState,
						modifier = Modifier.align(Alignment.BottomCenter)
					)
					// TODO [tva] other secondary controls also can be placed here
					if (cameraUiState.filtersMode == FiltersMode.REPLACE_BACK) {
						RoundedButton(
							painter = painterResource(R.drawable.ic_photo),
							modifier = Modifier
								.padding(24.dp)
								.align(Alignment.BottomStart),
							onClick = {
								photoPickerLauncher.launch(
									PickVisualMediaRequest(
										ActivityResultContracts.PickVisualMedia.ImageOnly
									)
								)
							}
						)
						RoundedButton(
							painter = painterResource(R.drawable.ic_clear),
							modifier = Modifier
								.padding(24.dp)
								.align(Alignment.BottomEnd),
							onClick = viewModel::removeBackground
						)
					}
				}
				BottomBar(
					cameraUiState = cameraUiState,
					onFlipCameraClick = viewModel::flipCamera,
					onCaptureClick = viewModel::captureImage,
					onLongPress = viewModel::startVideoRecording,
					onRelease = viewModel::stopVideoRecording,
					onFilterSettingClick = viewModel::setFilters
				)
			}
		}
	}
}

@Composable
private fun ImageButton(painter: Painter, onClick: () -> Unit) {
	IconButton(onClick = onClick) {
		Icon(
			painter = painter,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onPrimary
		)
	}
}

/**
 * Small floating semi transparent icon button
 */
@Composable
private fun RoundedButton(
	painter: Painter,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	IconButton(
		onClick = onClick,
		modifier = modifier
			.alpha(0.75f)
	) {
		Icon(
			painter = painter,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.surfaceDim,
			modifier = Modifier
				.size(48.dp)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(1f))
				.padding(4.dp)
		)
	}
}

@Composable
private fun EffectsCameraPreview(
	frame: Bitmap?,
	snackbarHostState: SnackbarHostState
){
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.onSurface),
	){

		if (frame == null){
			val snackbarMessage = stringResource(id = R.string.camera_not_ready)
			LaunchedEffect(snackbarHostState){
				snackbarHostState.showSnackbar(snackbarMessage)
			}
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				CircularProgressIndicator(
					modifier = Modifier.width(64.dp),
					color = MaterialTheme.colorScheme.surface,
				)
			}
		}
		else {
			Image(
				bitmap = frame.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.FillWidth,
				modifier = Modifier.fillMaxSize()
			)
		}
	}
}

@Composable
private fun TopBar(
	cameraUiState: CameraUiState,
	onToggleTopBar: (ExpandedTopBarMode) -> Unit,
	onFlashSettingClick: (FlashMode) -> Unit,
	onFilterSettingClick: (FiltersMode) -> Unit
){
	Row(
		horizontalArrangement = Arrangement.Absolute.SpaceAround,
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.Transparent)
	) {
		when(cameraUiState.expandedTopBarMode){
			ExpandedTopBarMode.FLASH -> {
				ImageButton(
					painter = painterResource(id = R.drawable.ic_flash_off),
					onClick = {
						onFlashSettingClick(FlashMode.OFF)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_flash_auto),
					onClick = {
						onFlashSettingClick(FlashMode.AUTO)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_flash_on),
					onClick = {
						onFlashSettingClick(FlashMode.ON)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
			}
			ExpandedTopBarMode.FILTERS -> {
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_blur),
					onClick = {
						onFilterSettingClick(FiltersMode.BLUR)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)

					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_background),
					onClick = {
						onFilterSettingClick(FiltersMode.REPLACE_BACK)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_beautify),
					onClick = {
						onFilterSettingClick(FiltersMode.BEAUTIFY)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_smart_zoom),
					onClick = {
						onFilterSettingClick(FiltersMode.SMART_ZOOM)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_color_correction),
					onClick = {
						onFilterSettingClick(FiltersMode.COLOR_CORRECTION)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_none),
					onClick = {
						onFilterSettingClick(FiltersMode.NONE)
						onToggleTopBar(ExpandedTopBarMode.DEFAULT)
					}
				)
			}
			ExpandedTopBarMode.SETTINGS -> {
				//TODO [fmv] expanded camera settings
			}
			ExpandedTopBarMode.DEFAULT -> {
				ImageButton( // flash
					painter = painterResource(when (cameraUiState.flashMode) {
						FlashMode.AUTO -> R.drawable.ic_flash_auto
						FlashMode.ON -> R.drawable.ic_flash_on
						FlashMode.OFF -> R.drawable.ic_flash_off
					}),
					onClick = { onToggleTopBar(ExpandedTopBarMode.FLASH) }
				)

				ImageButton( // filters
					painter = painterResource(R.drawable.ic_filter),
					onClick = {onToggleTopBar(ExpandedTopBarMode.FILTERS)}
				)

				ImageButton( // secondary options
					painter = painterResource(R.drawable.ic_more),
					onClick = {  }
				)
			}
		}

	}
}

@Composable
private fun BottomBar(
	cameraUiState: CameraUiState,
	onFlipCameraClick: () -> Unit,
	onCaptureClick: () -> Unit,
	onLongPress: () -> Unit,
	onRelease: () -> Unit,
	onFilterSettingClick: (FiltersMode) -> Unit
) {
	// 3-segmented row to keep camera button always centered
	Column {
		FiltersCarousel(onFilterSettingClick)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(Color.Transparent)
				.height(IntrinsicSize.Min),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {

			// segment left of capture button
			Row(
				modifier = Modifier.weight(1f)
			) {

			}
			CaptureButton(
				modifier = Modifier
					.fillMaxHeight()
					.width(IntrinsicSize.Min)
					.weight(1f),
				onClick = onCaptureClick,
				onLongPress = onLongPress,
				onRelease = onRelease,
				isVideoRecording = cameraUiState.isVideoRecording
			)

			// segment right of capture button
			Row(
				modifier = Modifier
					.weight(1f),
				horizontalArrangement = Arrangement.Center

			) {
				FlipCameraButton(
					modifier = Modifier
						.fillMaxHeight()
						.width(IntrinsicSize.Min)
						.weight(1f),
					onClick = onFlipCameraClick
				)
			}
		}
	}
}

@Composable
private fun CaptureButton(
	isVideoRecording: Boolean,
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	onLongPress: () -> Unit,
	onRelease: () -> Unit,
) {
	Box(
		modifier = modifier
			.fillMaxHeight()
			.pointerInput(Unit) {
				detectTapGestures(
					onLongPress = {
						onLongPress()
					},
					onPress = {
						awaitRelease()
						onRelease()
					},
					onTap = { onClick() }
				)
			}
			.requiredSize(120.dp)
			.padding(18.dp)
			.border(4.dp, Color.White, CircleShape)
	) {
		Canvas(modifier = Modifier.size(110.dp), onDraw = {
			drawCircle(
				color = if (isVideoRecording) Color.Red else Color.Transparent
			)
		})
	}
}

@Composable
private fun FlipCameraButton(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
) {
	IconButton(
		onClick = onClick,
		modifier = modifier
	) {
		Icon(
			imageVector = Icons.Filled.Refresh,
			tint = Color.White,
			contentDescription = null,
			modifier = Modifier.size(72.dp)
		)
	}
}

/**
 * Snackbar for this screen
 */
@Composable
private fun CameraSnackbar(
	snackbarHostState: SnackbarHostState,
	modifier: Modifier = Modifier,
){
	SnackbarHost(
		hostState = snackbarHostState,
		snackbar = { data ->
			Snackbar(
				modifier = Modifier
					.padding(10.dp),
				content = {
					Text(
						text = data.visuals.message,
					)
				},
			)
		},
		modifier = modifier
			.wrapContentHeight(Alignment.Bottom)
	)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FiltersCarousel(
	onPageChange: (FiltersMode) -> Unit
){

	val carouselState = rememberLazyListState()
	var xOffset = 0

	BoxWithConstraints(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyRow(
			//modifier = Modifier.fillMaxWidth(),
			state = carouselState,
			flingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState),
			horizontalArrangement = Arrangement.Center
		) {
			itemsIndexed(enumValues<FiltersMode>()) { index, mode ->
				Layout(
					content = {
						Text(
							modifier = Modifier
								.padding(horizontal = 16.dp),
							text = stringResource(id = mode.description),
							color = MaterialTheme.colorScheme.surface
						)
					},
					measurePolicy = { measurables, constraints ->
						val placeable = measurables.first().measure(constraints)
						val maxWidthInPx = maxWidth.roundToPx()
						val itemWidth = placeable.width
						xOffset = (maxWidthInPx - itemWidth) / 2
						val startSpace = if (index == 0) xOffset else 0
						val endSpace =
							if (index == enumValues<FiltersMode>().lastIndex) xOffset else 0
						val width = startSpace + placeable.width + endSpace
						layout(width, placeable.height) {
							val x = if (index == 0) startSpace else 0
							placeable.place(x, 0)
						}
					}
				)
			}
		}

		LaunchedEffect(carouselState){
			snapshotFlow { carouselState.layoutInfo.visibleItemsInfo }
				.distinctUntilChanged { old, new ->
					old.zip(new).all { (n1, n2) -> (compareValuesBy(n1, n2, {it.offset}, {it.index} ) == 0) }
				}
				.collect{ visibleItemsInfo ->
					val item = visibleItemsInfo.find { visibleItem ->
						val delta = visibleItem.size / 2
						val center = carouselState.layoutInfo.viewportEndOffset / 2
						val childCenter = visibleItem.offset + visibleItem.size / 2
						val target = childCenter - center
						target in -delta..delta
					}
					onPageChange(enumValues<FiltersMode>()[item!!.index])
				}

		}
	}
}