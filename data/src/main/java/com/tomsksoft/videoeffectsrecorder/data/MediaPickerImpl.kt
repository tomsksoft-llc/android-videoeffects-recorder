package com.tomsksoft.videoeffectsrecorder.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.tomsksoft.videoeffectsrecorder.domain.MediaPicker
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MediaPickerImpl(val context: Context): MediaPicker {
    companion object {
        private const val TAG = "MediaPickerImpl"
    }

    override val mediaList: BehaviorSubject<List<Any>> = BehaviorSubject.create()

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /*Content resolver query parameters*/
    private val projection: Array<String> = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.DURATION
    )
    private val selectionClause: String? = null
    private val selectionArgs: Array<String>? = null
    private val sortOrder: String? = null
    /*---*/

    private var cursor: Cursor? = null

    override fun loadVideos() {
        cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, // TODO: make this for both photo and video
            projection,
            selectionClause,
            selectionArgs,
            sortOrder
        )
        Log.d(TAG, "Query was called with ${cursor?.count} rows retrieved")
        mediaList.onNext(loadUriList(cursor))
    }

    @SuppressLint("Range")  // TODO [fmv] look into it later
    private fun loadUriList(cursor: Cursor?): List<Uri> {
        val uriList = mutableListOf<Uri>()
        cursor?.moveToFirst()
        while (cursor!!.moveToNext()) {
            val videoUri: Uri = ContentUris
                .withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)).toLong()
                )
            uriList.add(videoUri)
            Log.d(TAG, "${videoUri}")
        }
        return uriList
    }
}