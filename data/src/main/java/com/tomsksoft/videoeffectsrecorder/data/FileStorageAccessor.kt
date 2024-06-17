package com.tomsksoft.videoeffectsrecorder.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.OutputStream

internal class FileStorageAccessor(private val context: Context) {

    private val contentResolver: ContentResolver get() = context.contentResolver

    @SuppressLint("Recycle")
    fun createVideoFileDescriptor(
        filename: String,
        directoryName: String,
        mimeType: String
    ): Pair<ParcelFileDescriptor, Uri> {
        val unixMs = System.currentTimeMillis()
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Video.Media.DATE_ADDED, unixMs)
            put(MediaStore.Video.Media.DISPLAY_NAME, "${filename}_$unixMs")
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$directoryName")
        })!! // MediaStore concerns about making dirs and providing unique filenames on its own
        return contentResolver.openFileDescriptor(uri, "w")!! to uri
    }

    fun createImageFileStream(
        filename: String,
        directoryName: String,
        mimeType: String
    ): OutputStream {
        val unixMs = System.currentTimeMillis()
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Video.Media.DATE_ADDED, unixMs)
            put(MediaStore.Video.Media.DISPLAY_NAME, "${filename}_$unixMs")
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$directoryName")
        })!!
        return contentResolver.openOutputStream(uri)!!
    }

    fun deleteFile(file: Uri) {
        contentResolver.delete(file, null, null)
    }
}