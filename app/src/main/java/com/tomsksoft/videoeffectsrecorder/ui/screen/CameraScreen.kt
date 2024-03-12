package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.Manifest
import android.graphics.Bitmap
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.ui.toPx
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ICameraViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModelImpl
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModelStub
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ExpandedTopBarMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.FlashMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.SecondaryFiltersMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
	val context = LocalContext.current

	// permissions
	val permissionsLauncher = rememberMultiplePermissionsState(
		permissions = mutableListOf(
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO
		)
	) {
		if (it.values.any { granted -> !granted }) // any permission denied
			Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show()
	}
	LaunchedEffect(Unit) { permissionsLauncher.launchMultiplePermissionRequest() }

	// keep screen on
	(context as? ComponentActivity)?.run {
		DisposableEffect(Unit) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
		}
	}

	// ui
	CameraUi(viewModel<CameraViewModelImpl>())
}

@Preview
@Composable
fun CameraPreview() {
	CameraUi(CameraViewModelStub)
}

@Composable
fun CameraUi(viewModel: ICameraViewModel) {
	val context = LocalContext.current
	val frame by viewModel.frame.subscribeAsState(null)
	val cameraUiState: CameraUiState by viewModel.cameraUiState.collectAsState()
	val cameraConfig: CameraConfig by viewModel.cameraConfig.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
		if (uri != null)
			viewModel.setBackground(
				context.contentResolver.openInputStream(uri)!!
			) // TODO [tva] check on Android 9 or below
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
					snackbarHostState,
					viewModel::toggleQuickSettingsIndicator,
					viewModel::setFlash,
					viewModel::setSecondaryFilters,
				)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					Column(
						modifier = Modifier
							.fillMaxSize(),
						verticalArrangement = Arrangement.SpaceBetween
					) {
						SecondaryEffectsOptions(
							cameraUiState = cameraUiState,
							cameraConfig = cameraConfig,
							onBeautifySliderChange = viewModel::setBeautifyPower,
							onSmartZoomSliderChange = viewModel::setZoomPower,
							modifier = Modifier
						)
						Box(Modifier.weight(1f)) {
							CameraSnackbar(
								snackbarHostState = snackbarHostState,
								modifier = Modifier.align(Alignment.BottomCenter)
							)
						}
						PrimaryEffectsOptions(
							cameraUiState = cameraUiState,
							cameraConfig = cameraConfig,
							snackbarHostState = snackbarHostState,
							onPhotoPickClick = {
								photoPickerLauncher.launch(
									PickVisualMediaRequest(
										ActivityResultContracts.PickVisualMedia.ImageOnly
									)
								)
							},
							onRemoveClick = viewModel::removeBackground,
							onBlurSliderChange = viewModel::setBlurPower,
							onColorCorrectionModeChange = viewModel::setColorCorrectionMode
						)
					}
				}
				BottomBar(
					cameraUiState = cameraUiState,
					onFlipCameraClick = viewModel::flipCamera,
					onCaptureClick = viewModel::captureImage,
					onLongPress = viewModel::startVideoRecording,
					onRelease = viewModel::stopVideoRecording,
					onFilterSettingClick = viewModel::setPrimaryFilter
				)
			}
		}
	}
}

@Composable
fun PrimaryEffectsOptions(
	cameraUiState: CameraUiState,
	cameraConfig: CameraConfig,
	onPhotoPickClick: () -> Unit,
	onRemoveClick: () -> Unit,
	onBlurSliderChange: (Float) -> Unit,
	onColorCorrectionModeChange: (CameraConfig.ColorCorrection) -> Unit,
	snackbarHostState: SnackbarHostState
) {
	val scope = rememberCoroutineScope()

	Row(
		modifier = Modifier
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		when (cameraUiState.primaryFiltersMode) {
			PrimaryFiltersMode.REPLACE_BACK -> {
				RoundedButton(
					painter = painterResource(R.drawable.ic_photo),
					modifier = Modifier
						.padding(12.dp),
					onClick = onPhotoPickClick
				)
				RoundedButton(
					painter = painterResource(R.drawable.ic_clear),
					modifier = Modifier
						.padding(12.dp),
					onClick = onRemoveClick
				)
			}

			PrimaryFiltersMode.BLUR -> {
				Slider(
					value = cameraConfig.blurPower.toFloat(),
					onValueChange = onBlurSliderChange,
					modifier = Modifier
						.padding(6.dp)
				)
			}
			PrimaryFiltersMode.COLOR_CORRECTION -> {
					RoundedButton(
						painter = painterResource(R.drawable.ic_filter_color_correction),
						onClick = {
							onColorCorrectionModeChange(CameraConfig.ColorCorrection.COLOR_CORRECTION)
							scope.launch {
								snackbarHostState.showSnackbar("Color correction was selected")
							}
						},
						modifier = Modifier
							.padding(12.dp),
						backgroundColor =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.COLOR_CORRECTION) Color.Yellow
							else MaterialTheme.colorScheme.surface,
						tint =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.COLOR_CORRECTION) Color.Black
							else MaterialTheme.colorScheme.surfaceDim,
					)
					RoundedButton(
						painter = painterResource(R.drawable.ic_filter_color_grading),
						onClick = {
							onColorCorrectionModeChange(CameraConfig.ColorCorrection.COLOR_GRADING)
							scope.launch {
								snackbarHostState.showSnackbar("Color grading was selected")
							}
								  },
						modifier = Modifier
							.padding(12.dp),
						backgroundColor =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.COLOR_GRADING) Color.Yellow
							else MaterialTheme.colorScheme.surface,
						tint =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.COLOR_GRADING) Color.Black
							else MaterialTheme.colorScheme.surfaceDim

					)
					RoundedButton(
						painter = painterResource(R.drawable.ic_filter_preset),
						onClick = {
							onColorCorrectionModeChange(CameraConfig.ColorCorrection.PRESET)
							scope.launch {
								snackbarHostState.showSnackbar("Preset was selected")
							}
						},
						modifier = Modifier
							.padding(12.dp),
						backgroundColor =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.PRESET) Color.Yellow
							else MaterialTheme.colorScheme.surface,
						tint =
							if (cameraConfig.colorCorrection == CameraConfig.ColorCorrection.PRESET) Color.Black
							else MaterialTheme.colorScheme.surfaceDim
					)
			}
			PrimaryFiltersMode.NONE -> {}
		}
	}
}

@Composable
fun SecondaryEffectsOptions(
	cameraUiState: CameraUiState,
	cameraConfig: CameraConfig,
	onBeautifySliderChange: (Float) -> Unit,
	onSmartZoomSliderChange: (Float) -> Unit,
	modifier: Modifier
) {
	Column(
		modifier = modifier
		//Modifier.align(Alignment.TopCenter)
	) {
		if (cameraUiState.isBeautifyEnabled) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_filter_beautify),
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier
						.weight(1f)
						.padding(3.dp)
				)
				Slider(
					onValueChange = onBeautifySliderChange,
					value = ((cameraConfig.beautification ?: 0)/100f),
					modifier = Modifier
						.weight(3f)
						.padding(3.dp)
				)
			}
		}
		if (cameraUiState.isSmartZoomEnabled) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_filter_smart_zoom),
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier
						.weight(1f)
						.padding(3.dp)
				)
				Slider(
					value = (cameraConfig.smartZoom ?: 0)/100f,
					onValueChange = onSmartZoomSliderChange,
					modifier = Modifier
						.weight(3f)
						.padding(3.dp)
				)
			}
		}
	}
}

@Composable
private fun ImageButton(painter: Painter, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onPrimary) {
	IconButton(onClick = onClick) {
		Icon(
			painter = painter,
			contentDescription = null,
			tint = tint
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
	modifier: Modifier = Modifier,
	backgroundColor: Color = MaterialTheme.colorScheme.surface,
	tint: Color = MaterialTheme.colorScheme.surfaceDim
) {
	IconButton(
		onClick = onClick,
		modifier = modifier
			.alpha(0.75f)
	) {
		Icon(
			painter = painter,
			contentDescription = null,
			tint = tint,
			modifier = Modifier
				.size(48.dp)
				.background(backgroundColor, RoundedCornerShape(1f))
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
	snackbarHostState: SnackbarHostState,
	onToggleTopBar: (ExpandedTopBarMode) -> Unit,
	onFlashSettingClick: (FlashMode) -> Unit,
	onFilterSettingClick: (SecondaryFiltersMode) -> Unit
){
	val scope = rememberCoroutineScope()
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

				// ---> two secondary filters options
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_beautify),
					onClick = {
						onFilterSettingClick(SecondaryFiltersMode.BEAUTIFY)
						if (!cameraUiState.isBeautifyEnabled) {
							scope.launch {
								snackbarHostState.showSnackbar("Beautify enabled")
							}
						}
					},
					tint = if (cameraUiState.isBeautifyEnabled) Color.Yellow else MaterialTheme.colorScheme.onPrimary
				)

				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_smart_zoom),
					onClick = {
						onFilterSettingClick(SecondaryFiltersMode.SMART_ZOOM)
						if (!cameraUiState.isSmartZoomEnabled) {
							scope.launch {
								snackbarHostState.showSnackbar("Smart Zoom enabled")
							}
						}
					},
					tint = if (cameraUiState.isSmartZoomEnabled) Color.Yellow else MaterialTheme.colorScheme.onPrimary
				)
				// <--- two secondary filters options

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
	onFilterSettingClick: (PrimaryFiltersMode) -> Unit
) {
	Column {
		FiltersCarousel(
			primaryFiltersModeSelected = cameraUiState.primaryFiltersMode,
			onFilterSelected = onFilterSettingClick
		)
		// 3-segmented row to keep camera button always centered
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
fun FiltersCarousel (
	primaryFiltersModeSelected: PrimaryFiltersMode,
	onFilterSelected: (PrimaryFiltersMode) -> Unit
) {
	val carouselState = rememberLazyListState(Int.MAX_VALUE / 2)
	val scope = rememberCoroutineScope()
	val filters = enumValues<PrimaryFiltersMode>()
	val itemsCount = Int.MAX_VALUE
	val snappingLayout = remember(carouselState) { SnapLayoutInfoProvider(carouselState) }
	val snapFlingBehavior : FlingBehavior = rememberSnapFlingBehavior(snappingLayout)


	// TODO [fmv] snap initial item to the center of the screen

	// TODO [fmv] add ability to launch filter selection callback at the end of the snapping animation
	LazyRow (
		state = carouselState,
		flingBehavior = snapFlingBehavior,
	) {
		items (
			count = itemsCount
		) {index ->
			val horizontalPadding = 10.dp
			var textSize by remember { mutableStateOf(IntSize.Zero)	}
			Text(
				text = stringResource(id = filters[index % filters.size].description),
				color = if (filters[index % filters.size] == primaryFiltersModeSelected) Color.Yellow
						else MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier
					.padding(horizontal = horizontalPadding)
					.onSizeChanged { textSize = it }
					.clickable {
						onFilterSelected(filters[index % filters.size])
						scope.launch {
							carouselState.animateScrollToItem(
								index = index,
								// center of the lazyrow viewport is calculated; text size and padding being subtracted
								scrollOffset = -((carouselState.layoutInfo.viewportEndOffset / 2) - textSize.width / 2 - horizontalPadding.value.toPx())
							)
						}
					}
			)
		}
	}

}