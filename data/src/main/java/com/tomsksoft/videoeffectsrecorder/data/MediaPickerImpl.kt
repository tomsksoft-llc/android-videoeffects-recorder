package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
import com.tomsksoft.videoeffectsrecorder.domain.entity.Media

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

    /**
     * Loads media from the device's external storage.
     *
     * This method queries the MediaStore for both video and image media, merges the results into a single cursor,
     * and then returns the media as a list of [Media] objects.
     *
     * @return a list of [Media] objects representing the media found on the device.
     */
    override fun loadMedia(): List<Media> {
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

        // Create a list of Media objects using cursor
        return loadMediaList(cursor)
    }
    /**
     * Loads media from the given cursor and returns a list of Media objects.
     *
     * @param cursor The cursor containing the media data.
     * @return list of Media objects representing the media found in the cursor.
     */
    @Throws(Exception::class)
    private fun loadMediaList(cursor: Cursor?): List<Media> {
        val mediaList = mutableListOf<Media>()
        cursor?.moveToFirst()
        while (cursor!!.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            var isVideo: Boolean
            // Determine URI path based on media type
            var contentUri: Uri
            if (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)).contains("image")) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                isVideo = false
            } else if (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)).contains("video")) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                isVideo = true
            } else continue // if there's no "image" or "video" in URI, then probably something is wrong with file
            // Append the media ID to the URI path
            val mediaUri: Uri = ContentUris
                .withAppendedId(
                    contentUri,
                    id
                )
            val mediaLabel: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))

            val mediaItem = Media(
                id = id,
                uri = mediaUri.toString(),
                label = mediaLabel,
                isVideo = isVideo
            )
            mediaList.add(mediaItem)
            Log.d(TAG, "$mediaItem")
        }
        return mediaList
    }
}