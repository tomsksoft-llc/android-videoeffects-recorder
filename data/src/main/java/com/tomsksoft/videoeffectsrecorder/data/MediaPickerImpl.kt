package com.tomsksoft.videoeffectsrecorder.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker

class MediaPickerImpl(val context: Context): MediaPicker {
    companion object {
        private const val TAG = "MediaPickerImpl"
    }

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /* Content resolver query parameters */
    private val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DURATION,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.DATE_TAKEN
    )
    private val selectionClause: String? = null
    private val selectionArgs: Array<String>? = null
    private val sortOrder: String? = null
    /*---*/

    private var cursor: Cursor? = null

    override fun loadVideos(): List<String> {
        cursor = MergeCursor(
            arrayOf(
                contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selectionClause,
                    selectionArgs,
                    sortOrder
                ),
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selectionClause,
                    selectionArgs,
                    sortOrder
                )
            )
        )
        Log.d(TAG, "Query was called with ${cursor?.count} rows retrieved")
        return loadUriList(cursor).map(Uri::toString)
    }

    override fun deleteVideos(uriList: List<String>) {
        for (i in uriList) contentResolver.delete(Uri.parse(i), null, null)
    }

    @SuppressLint("Range")  // TODO [fmv] look into it later
    private fun loadUriList(cursor: Cursor?): List<Uri> {
        val uriList = mutableListOf<Uri>()
        cursor?.moveToFirst()
        while (cursor!!.moveToNext()) {
            var contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            if (cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)).contains("image"))
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else if (cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)).contains("video"))
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val videoUri: Uri = ContentUris
                .withAppendedId(
                    contentUri,
                    cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID)).toLong()
                )
            uriList.add(videoUri)
            Log.d(TAG, "$videoUri")
        }
        return uriList
    }
}