package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel

@Preview(widthDp = 450, heightDp = 800, showBackground = true)
@Composable
fun CameraScreen(
	@PreviewParameter(CameraViewModelProvider::class) viewModel: CameraViewModel
) {
	val flashMode by viewModel.flashMode.collectAsState()
	val frame by viewModel.frame.collectAsState()
	Column(modifier = Modifier.fillMaxHeight()) {
		Row(
			horizontalArrangement = Arrangement.Absolute.SpaceAround,
			modifier = Modifier
				.fillMaxWidth(1f)
				.wrapContentHeight(Alignment.Top)
				.background(MaterialTheme.colorScheme.primary)
				.padding(vertical = 8.dp)
		) {
			ImageButton( // flash
				painter = painterResource(when (flashMode) {
					CameraViewModel.FlashMode.AUTO -> R.drawable.ic_flash_auto
					CameraViewModel.FlashMode.ON -> R.drawable.ic_flash_on
					CameraViewModel.FlashMode.OFF -> R.drawable.ic_flash_off
				}),
				onClick = { viewModel.changeFlashMode() }
			)
			ImageButton( // filters
				painter = painterResource(R.drawable.ic_filter),
				onClick = { /* TODO [tva] show filters */ }
			)
			ImageButton( // secondary options
				painter = painterResource(R.drawable.ic_more),
				onClick = { /* TODO [tva] show popup options */ }
			)

		}
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.onSurface)
		) {
			if (frame == null)
				Column(
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Icon(
						painter = painterResource(R.drawable.ic_camera),
						tint = MaterialTheme.colorScheme.surface,
						modifier = Modifier.size(72.dp),
						contentDescription = null
					)
					Spacer(modifier = Modifier.height(12.dp))
					Text(
						text = "Нет доступа к камере",
						color = MaterialTheme.colorScheme.surface,
						fontSize = 16.sp
					)
				}
			else Image(
				bitmap = frame!!.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.FillWidth,
				modifier = Modifier.fillMaxSize()
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

class CameraViewModelProvider: PreviewParameterProvider<CameraViewModel> {
	override val values: Sequence<CameraViewModel>
		get() = sequenceOf(CameraViewModel())
}