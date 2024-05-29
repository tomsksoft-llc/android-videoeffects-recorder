package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.ImagePipeline
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaProcessor

class MediaProcessorImpl(
    private val context: Context
): MediaProcessor {
    companion object {
        private const val TAG = "MediaProcessor"
        private val factory = EffectsSDK.createSDKFactory()
    }
    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /*override */private var image: Bitmap? = null//BehaviorSubject.create<Any>()
    private var pipeline: ImagePipeline? = null
    private var surface: Surface? = null
    private fun createImagePipeline() {
        pipeline = factory.createImagePipeline(
            context
        )
        //pipeline.setOutputSurface(surface)
    }

    /*override fun onNewFrame(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }*/

    override fun addMedia(uri: String) {
        //load image from external storage
        Log.d(TAG, uri)
        image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri.toUri()))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri.toUri())
        }
        // create pipeline for it
        createImagePipeline()
    }

    override fun setSurface(surface: Surface?) {
        this.surface = surface
        pipeline?.setOutputSurface(this.surface)
        pipeline?.process(image!!.copy(Bitmap.Config.ARGB_8888, true))
        //pipeline?.process(image!!)
        //pipeline?.process(image!!)
    }
    override fun processImage() {
        pipeline?.process(image!!)
    }
}