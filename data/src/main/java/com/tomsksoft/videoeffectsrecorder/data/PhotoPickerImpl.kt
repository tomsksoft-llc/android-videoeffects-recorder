package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.rotationMatrix
import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker

class PhotoPickerImpl(private val context: Context, private val directoryName: String):
    PhotoPicker {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /**
     * Blocking
     */
    override fun takePhoto(frame: Any, orientation: Int, filename: String, mimeType: String) {
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$directoryName")
        })!!
        contentResolver.openOutputStream(uri)!!.use { stream ->
            val rawPhoto = frame as Bitmap // rotated as is
            val photo = Bitmap.createBitmap(
                rawPhoto, 0, 0, rawPhoto.width, rawPhoto.height,
                rotationMatrix(orientation.toFloat()), true
            )
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
}