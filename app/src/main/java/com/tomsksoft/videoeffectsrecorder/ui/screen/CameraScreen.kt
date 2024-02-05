package com.tomsksoft.videoeffectsrecorder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.CameraViewModel

@Preview(widthDp = 450, heightDp = 800, showBackground = true)
@Composable
fun CameraScreen(
	@PreviewParameter(CameraViewModelProvider::class) viewModel: CameraViewModel
) {
	Row(
		horizontalArrangement = Arrangement.Absolute.SpaceAround,
		modifier = Modifier
			.fillMaxWidth(1f)
			.wrapContentHeight(Alignment.Top)
			.background(MaterialTheme.colorScheme.primary)
			.padding(vertical = 8.dp)
	) {
		val flashMode by remember { viewModel.flashMode }
		ImageButton( // flash
			painter = painterResource(when (viewModel.flashMode.value) {
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