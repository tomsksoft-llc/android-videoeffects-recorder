package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.Direction
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.SecondaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.EditMediaViewModel
import kotlinx.coroutines.launch

@Composable
fun EditMediaScreen(
    viewModel: EditMediaViewModel,
    uri: String?
) {
    var primaryFiltersMode by remember { mutableStateOf(PrimaryFiltersMode.NONE) }
    val cameraConfig by viewModel.cameraConfig.subscribeAsState(initial = CameraConfig())

    LaunchedEffect(Unit) {
        viewModel.addMedia(uri!!)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { viewModel.processImage() }
    ) {
        EffectsCameraPreview(
            viewModel::updateSurface
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

            }
            BottomBar(editMediaScreenState = primaryFiltersMode, cameraConfig = cameraConfig) {
                Log.d("EditMediaScreen", "$it")
                viewModel.setPrimaryFilter(it)
                primaryFiltersMode = it
            }
        }
        /*AndroidView(
            factory = { context ->
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.preview, FrameLayout(context), false)
                        as SurfaceView

                view.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        viewModel.updateSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) = Unit

                    override fun surfaceDestroyed(holder: SurfaceHolder) = viewModel.updateSurface(null)
                })
                view
            }
        )*/
    }
}

@Composable
private fun TopBar(
    cameraConfig: CameraConfig,
    onFilterSettingClick: (SecondaryFiltersMode) -> Unit
){
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.Absolute.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        // ---> three secondary filters options
        ImageButton(
            painter = painterResource(id = R.drawable.ic_filter_beautify),
            onClick = {
                onFilterSettingClick(SecondaryFiltersMode.BEAUTIFY)
                /*if (cameraConfig.beautification == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Beautify enabled")
                    }
                }*/
            },
            tint = if (cameraConfig.beautification != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
        )

        ImageButton(
            painter = painterResource(id = R.drawable.ic_filter_smart_zoom),
            onClick = {
                onFilterSettingClick(SecondaryFiltersMode.SMART_ZOOM)
                /*if (cameraConfig.smartZoom == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Smart Zoom enabled")
                    }
                }*/
            },
            tint = if (cameraConfig.smartZoom != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
        )

        ImageButton(
            painter = painterResource(id = R.drawable.ic_filter),
            onClick = {
                onFilterSettingClick(SecondaryFiltersMode.SHARPNESS)
                /*if (cameraConfig.sharpnessPower == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Sharpness enabled")
                    }
                }*/
            },
            tint = if (cameraConfig.sharpnessPower != null) Color.Yellow else MaterialTheme.colorScheme.onPrimary
        )
        // <--- three secondary filters options
    }
}

@Composable
private fun BottomBar(
    editMediaScreenState: PrimaryFiltersMode,
    cameraConfig: CameraConfig,
    onFilterSettingClick: (PrimaryFiltersMode) -> Unit
) {
    Column {
        FiltersCarousel(
            primaryFiltersModeSelected = editMediaScreenState,
            onFilterSelected = onFilterSettingClick
        )
    }
}