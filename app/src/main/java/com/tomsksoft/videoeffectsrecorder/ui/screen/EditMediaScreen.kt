package com.tomsksoft.videoeffectsrecorder.ui.screen

import android.net.Uri
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.tomsksoft.videoeffectsrecorder.R
import com.tomsksoft.videoeffectsrecorder.ui.viewmodel.EditMediaViewModel

@Composable
fun EditMediaScreen(
    viewModel: EditMediaViewModel,
    uri: String?
) {
    LaunchedEffect(Unit) {
        viewModel.addMedia(uri!!)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { viewModel.processImage() }
    ) {
        AndroidView(
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
        )
    }
}