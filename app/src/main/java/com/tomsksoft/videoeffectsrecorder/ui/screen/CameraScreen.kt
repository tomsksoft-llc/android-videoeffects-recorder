package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.Manifest
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.annotation.IntDef
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import com.tomsksoft.videoeffectsrecorder.ui.toPx
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ICameraViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModelImpl
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModelStub
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ExpandedTopBarMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.SecondaryFiltersMode
import kotlinx.coroutines.launch

private const val REQUEST_PICK_PHOTO_BACKGROUND = 1
private const val REQUEST_PICK_PHOTO_GRADING_SOURCE = 2
@IntDef(REQUEST_PICK_PHOTO_BACKGROUND, REQUEST_PICK_PHOTO_GRADING_SOURCE)
private annotation class PickPhotoCode

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
	CameraUi(hiltViewModel<CameraViewModelImpl>())
}

@Preview
@Composable
fun CameraPreview() {
	CameraUi(CameraViewModelStub)
}

@Composable
fun CameraUi(viewModel: ICameraViewModel) {
	val context = LocalContext.current
	val cameraUiState: CameraUiState by viewModel.cameraUiState.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }

	/* Photo Picker */
	var pickPhotoRequestCode: Int? = remember { null }
	val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
		if (uri == null) return@rememberLauncherForActivityResult // skip if user cancelled pick

		val stream = context.contentResolver.openInputStream(uri)!!
		when (pickPhotoRequestCode) {
			REQUEST_PICK_PHOTO_BACKGROUND ->
				viewModel.setBackground(stream)
			REQUEST_PICK_PHOTO_GRADING_SOURCE ->
				viewModel.setColorCorrectionMode(ColorCorrection.COLOR_GRADING, stream)
		}
	}
	// animation of taking photo
	val alphaAnimation = remember { Animatable(0f) }
	val alpha by alphaAnimation.asState()
	val scope = rememberCoroutineScope()
	fun pickPhoto(@PickPhotoCode requestCode: Int) {
		pickPhotoRequestCode = requestCode
		photoPickerLauncher.launch(
			PickVisualMediaRequest(
				ActivityResultContracts.PickVisualMedia.ImageOnly
			)
		)
	}
	/* --- */

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
			EffectsCameraPreview(snackbarHostState, viewModel::setSurface)
			// black box for taking photo animation
			Box(modifier = Modifier
				.matchParentSize()
				.background(Color(0f, 0f, 0f, alpha)))
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
							onBeautifySliderChange = viewModel::setBeautifyPower,
							onSmartZoomSliderChange = viewModel::setZoomPower,
							onSharpnessSliderChange = viewModel::setSharpnessPower,
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
							snackbarHostState = snackbarHostState,
							onPickPhotoClick = { pickPhoto(REQUEST_PICK_PHOTO_BACKGROUND) },
							onRemoveClick = viewModel::removeBackground,
							onBlurSliderChange = viewModel::setBlurPower,
							onColorCorrectionModeChange = { mode ->
								if (mode == ColorCorrection.COLOR_GRADING)
									pickPhoto(REQUEST_PICK_PHOTO_GRADING_SOURCE)
								else
									viewModel.setColorCorrectionMode(mode)
							},
							onColorCorrectionSliderChange = viewModel::setColorCorrectionPower
						)
					}
				}
				BottomBar(
					cameraUiState = cameraUiState,
					onFlipCameraClick = viewModel::flipCamera,
					onCaptureClick = {
						scope.launch {
							val durationMs = 200
							alphaAnimation.animateTo(0f, snap())
							alphaAnimation.animateTo(1f, tween(durationMs / 2))
							alphaAnimation.animateTo(0f, tween(durationMs / 2))
						}
						viewModel.captureImage()
					},
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
	onPickPhotoClick: () -> Unit,
	onRemoveClick: () -> Unit,
	onBlurSliderChange: (Float) -> Unit,
	onColorCorrectionSliderChange: (Float) -> Unit,
	onColorCorrectionModeChange: (ColorCorrection) -> Unit,
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
					onClick = onPickPhotoClick
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
					value = cameraUiState.blur,
					onValueChange = onBlurSliderChange,
					modifier = Modifier
						.padding(6.dp)
				)
			}
			PrimaryFiltersMode.COLOR_CORRECTION -> {
				when(cameraUiState.colorCorrectionMode) {
					// show 3 default buttons for 3 available color correction modes (no one chosen so far)
					ColorCorrection.NO_FILTER -> {
						RoundedButton(
							painter = painterResource(R.drawable.ic_filter_color_correction),
							onClick = {
								onColorCorrectionModeChange(ColorCorrection.COLOR_CORRECTION)
								scope.launch {
									snackbarHostState.showSnackbar("Color correction was selected")
								}
							},
							modifier = Modifier
								.padding(12.dp),
							backgroundColor = MaterialTheme.colorScheme.surface,
							tint = MaterialTheme.colorScheme.surfaceDim
						)
						RoundedButton(
							painter = painterResource(R.drawable.ic_filter_color_grading),
							onClick = {
								onColorCorrectionModeChange(ColorCorrection.COLOR_GRADING)
								scope.launch {
									snackbarHostState.showSnackbar("Color grading was selected")
								}
							},
							modifier = Modifier
								.padding(12.dp),
							backgroundColor = MaterialTheme.colorScheme.surface,
							tint = MaterialTheme.colorScheme.surfaceDim
						)
						RoundedButton(
							painter = painterResource(R.drawable.ic_filter_preset),
							onClick = {
								onColorCorrectionModeChange(ColorCorrection.PRESET)
								scope.launch {
									snackbarHostState.showSnackbar("Preset was selected")
								}
							},
							modifier = Modifier
								.padding(12.dp),
							backgroundColor = MaterialTheme.colorScheme.surface,
							tint = MaterialTheme.colorScheme.surfaceDim
						)
					}
					// show slider for color correction
					else -> {
						RoundedButton(
							painter = painterResource(when (cameraUiState.colorCorrectionMode) {
								ColorCorrection.NO_FILTER -> R.drawable.ic_clear	// added this so 'when' statement stays exhaustive
								ColorCorrection.COLOR_CORRECTION -> R.drawable.ic_filter_color_correction
								ColorCorrection.COLOR_GRADING -> R.drawable.ic_filter_color_grading
								ColorCorrection.PRESET -> R.drawable.ic_filter_preset
							}),
							onClick = {
								onColorCorrectionModeChange(ColorCorrection.NO_FILTER)
							},
							modifier = Modifier
								.padding(12.dp),
							backgroundColor = Color.Yellow,
							tint = Color.Black
						)
						Slider(
							onValueChange = onColorCorrectionSliderChange,
							value = cameraUiState.colorCorrectionPower,
							modifier = Modifier
								.weight(3f)
								.padding(12.dp)
						)
					}
				}
			}
			PrimaryFiltersMode.NONE -> {}
		}
	}
}

@Composable
fun SecondaryEffectsOptions(
	cameraUiState: CameraUiState,
	onBeautifySliderChange: (Float) -> Unit,
	onSmartZoomSliderChange: (Float) -> Unit,
	onSharpnessSliderChange: (Float) -> Unit,
	modifier: Modifier
) {
	Column(
		modifier = modifier
		//Modifier.align(Alignment.TopCenter)
	) {
		if (cameraUiState.beautification != null) {
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
					value = cameraUiState.beautification / 100f,
					modifier = Modifier
						.weight(3f)
						.padding(3.dp)
				)
			}
		}
		if (cameraUiState.smartZoom != null) {
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
					value = cameraUiState.smartZoom / 100f,
					onValueChange = onSmartZoomSliderChange,
					modifier = Modifier
						.weight(3f)
						.padding(3.dp)
				)
			}
		}
		if (cameraUiState.sharpnessPower != null){
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_filter),
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier
						.weight(1f)
						.padding(3.dp)
				)
				Slider(
					value = cameraUiState.sharpnessPower,
					onValueChange = onSharpnessSliderChange,
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
	snackbarHostState: SnackbarHostState,
	updateSurface: (Surface?) -> Unit
){
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.onSurface),
	){
		AndroidView(
			factory = { context ->
				val view = LayoutInflater.from(context)
					.inflate(R.layout.preview, FrameLayout(context), false)
						as SurfaceView

				view.holder.addCallback(object : SurfaceHolder.Callback {
					override fun surfaceCreated(holder: SurfaceHolder) {
						updateSurface(holder.surface)
					}

					override fun surfaceChanged(
						holder: SurfaceHolder,
						p1: Int,
						p2: Int,
						p3: Int
					) = Unit

					override fun surfaceDestroyed(holder: SurfaceHolder) = updateSurface(null)
				})
				view
			}
		)
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

				// ---> three secondary filters options
				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_beautify),
					onClick = {
						onFilterSettingClick(SecondaryFiltersMode.BEAUTIFY)
						if (cameraUiState.beautification == null) {
							scope.launch {
								snackbarHostState.showSnackbar("Beautify enabled")
							}
						}
					},
					tint = if (cameraUiState.beautification != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
				)

				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter_smart_zoom),
					onClick = {
						onFilterSettingClick(SecondaryFiltersMode.SMART_ZOOM)
						if (cameraUiState.smartZoom == null) {
							scope.launch {
								snackbarHostState.showSnackbar("Smart Zoom enabled")
							}
						}
					},
					tint = if (cameraUiState.smartZoom != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
				)

				ImageButton(
					painter = painterResource(id = R.drawable.ic_filter),
					onClick = {
						onFilterSettingClick(SecondaryFiltersMode.SHARPNESS)
						if (cameraUiState.sharpnessPower == null) {
							scope.launch {
								snackbarHostState.showSnackbar("Sharpness enabled")
							}
						}
					},
					tint = if (cameraUiState.sharpnessPower != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
				)
				// <--- three secondary filters options

				/*ImageButton( // TODO [tva] add secondary options
					painter = painterResource(R.drawable.ic_more),
					onClick = {  }
				)*/
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
				text = stringResource(id = filters[index % filters.size].resourceId),
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