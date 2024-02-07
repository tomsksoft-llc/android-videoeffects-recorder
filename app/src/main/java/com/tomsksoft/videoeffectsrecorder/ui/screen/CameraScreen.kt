package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.FiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.FlashMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.ExpandedTopBarMode

@Preview(widthDp = 450, heightDp = 800, showBackground = true)
@Composable
fun CameraScreen(
	@PreviewParameter(CameraViewModelProvider::class) viewModel: CameraViewModel
) {
	val cameraUiState: CameraUiState by viewModel.cameraUiState.collectAsState()
	val frame by remember { viewModel.frame	}
	Box(
		modifier = Modifier
	){
		// effects sdk camera feed; stays behind all other elements
		EffectsCameraPreview(frame)

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
			BottomBar(
				cameraUiState,
				viewModel::flipCamera,
				viewModel::captureImage,
				viewModel::startVideoRecording,
				viewModel::stopVideoRecording
			)
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

@Composable
private fun EffectsCameraPreview(
	frame: Bitmap?
){
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.onSurface),
	){
		if (frame == null){
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					modifier = Modifier,
					text = "Нет доступа к камере",
					color = MaterialTheme.colorScheme.surface,
					fontSize = 16.sp,
				)
			}
		}
		else {
			Image(bitmap = frame.asImageBitmap(), contentDescription = null)
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
	onRelease: () -> Unit
) {
	// 3-segmented row to keep camera button always centered
	Column {
		Row(
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
		) {
			Text(
				color = MaterialTheme.colorScheme.surface,
				text = cameraUiState.filtersMode.color
			)
		}
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
				onRelease = onRelease
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
fun CaptureButton(
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
				color = Color.Transparent
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
	) {
		Icon(
			imageVector = Icons.Filled.Refresh,
			tint = Color.White,
			contentDescription = null,
			modifier = Modifier.size(72.dp)
		)
	}
}

class CameraViewModelProvider: PreviewParameterProvider<CameraViewModel> {
	override val values: Sequence<CameraViewModel>
		get() = sequenceOf(CameraViewModel())
}