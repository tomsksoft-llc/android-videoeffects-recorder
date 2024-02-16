package com.tomsksoft.videoeffectsrecorder.data

import com.tomsksoft.videoeffectsrecorder.domain.FileStore
import java.io.File

class VideoStore(
    override val directory: File
): FileStore {
    override fun getList(): List<File> = directory.listFiles()!!.toList()
        /*context.contentResolver.query(
            MediaStore.Files.getContentUri(),
            arrayOf(MediaStore.Files.FileColumns.DATA),
            null, null, null
        )?.use { cursor ->
            val list = ArrayList<File>()
            val dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            cursor.moveToFirst()

            do { list += File(cursor.getString(dataIndex)) } while (cursor.moveToNext())

            list
        } ?: throw RuntimeException("Couldn't get list of records")*/

    override fun create(filename: String, mimeType: String): File {
        return File(directory, filename)
        /*val contentValues = ContentValues(3).apply {
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, directory.toRelativeString(Environment.getExternalStorageDirectory()))
            put(MediaStore.Video.Media.TITLE, filename)
        }
        val uriToVideo = context.contentResolver.insert(directory.toUri(), contentValues)!!
        return context.contentResolver.openFileDescriptor(uriToVideo, "w")?.fileDescriptor
            ?: throw RuntimeException("Couldn't obtain scoped storage grant")*/
    }
}