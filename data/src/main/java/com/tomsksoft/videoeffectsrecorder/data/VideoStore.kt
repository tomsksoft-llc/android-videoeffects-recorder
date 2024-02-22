package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.tomsksoft.videoeffectsrecorder.domain.FileStore
import java.io.File
import java.util.Objects
import java.util.stream.Stream

/**
 * Used to have access to file system via {@link android.provider.MediaStore}
 * on Android 10 and above, or direct access on Android 9 and below.
 */
class VideoStore(
    private val context: Context,
    private val directoryName: String
): FileStore<ParcelFileDescriptor> {

    private val directory: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            directoryName
        ).also(File::mkdirs)
    }

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override fun create(
        filename: String,
        extension: String,
        mimeType: String
    ): ParcelFileDescriptor =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            ParcelFileDescriptor.open(
                createFile(filename, extension),
                ParcelFileDescriptor.MODE_WRITE_ONLY
            )
        else
            createFileDescriptor(filename, mimeType)

    private fun createFileDescriptor(filename: String, mimeType: String): ParcelFileDescriptor {
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$directoryName")
        })!! // MediaStore concerns about making dirs and providing unique filenames on its own
        return contentResolver.openFileDescriptor(uri, "w")!!
    }

    private fun createFile(filename: String, extension: String): File =
        File(directory, "$filename.$extension")
            .takeUnless(File::exists)
            ?: createIndexedFile(filename, extension)

    private fun createIndexedFile(baseName: String, extension: String): File {
        val sortedTakenIndices: IntArray = Stream.of(*directory.listFiles())
            .filter { it.extension == extension }
            .map(File::nameWithoutExtension)
            .filter { it.startsWith("${baseName}_") }
            .map { it.substring(baseName.length + 1).toIntOrNull() }
            .filter(Objects::nonNull)
            .mapToInt { it!! }
            .filter { it >= 0 }
            .sorted()
            .toArray()

        var minFreeIndex = 0

        while (
            minFreeIndex < sortedTakenIndices.size
            && sortedTakenIndices[minFreeIndex] == minFreeIndex
        ) minFreeIndex++

        return File("${baseName}_$minFreeIndex.$extension")
    }
}