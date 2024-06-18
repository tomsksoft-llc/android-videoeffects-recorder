package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.rotationMatrix
import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker

class PhotoPickerImpl(context: Context, private val directoryName: String): PhotoPicker {

    private val fileStorageAccessor = FileStorageAccessor(context)

    /**
     * Blocking
     */
    override fun takePhoto(frame: Any, orientation: Int, filename: String, mimeType: String) {
        fileStorageAccessor.createImageFileStream(filename, directoryName, mimeType).use { stream ->
            val rawPhoto = frame as Bitmap // rotated as is
            val photo = Bitmap.createBitmap(
                rawPhoto, 0, 0, rawPhoto.width, rawPhoto.height,
                rotationMatrix(orientation.toFloat()), true
            )
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
}