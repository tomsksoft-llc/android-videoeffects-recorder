package com.tomsksoft.videoeffectsrecorder.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.File
import java.util.Objects
import java.util.stream.Stream

class VideoRecorderImpl(
    private val context: Context,
    private val directoryName: String
): VideoRecorder {

    override val frame: BehaviorSubject<Any> = BehaviorSubject.create()
    override val degree: BehaviorSubject<Int> = BehaviorSubject.create()

    private val directory: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            directoryName
        ).also(File::mkdirs)
    }

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override fun startRecord(filename: String, extension: String, mimeType: String): AutoCloseable =
        Record(createOutput(filename, extension, mimeType))

    private fun createOutput(
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

    private inner class Record(
        private val outputFile: ParcelFileDescriptor
    ): AutoCloseable {

        private val mediaRecorder = (
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                    MediaRecorder()
                else
                    MediaRecorder(context)
            )
        @Volatile
        private var surface: Surface? = null
        private var disposableFrameSubscription: Disposable? = null

        init {
            disposableFrameSubscription = frame.observeOn(Schedulers.io()).subscribe { frame ->
                val bitmap = FrameMapper.fromAny(frame)
                val (width, height) = bitmap.width to bitmap.height
                // first frame setups MediaRecorder with appropriate video size and orientation
                if (surface == null)
                    start(width, height)
                val canvas = surface!!.lockCanvas(
                    Rect(0, 0, width, height)
                ) ?: return@subscribe
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                surface!!.unlockCanvasAndPost(canvas)
            }
        }

        override fun close() {
            disposableFrameSubscription?.dispose()
            mediaRecorder.stop()
            mediaRecorder.release()
            outputFile.close()
        }

        private fun start(width: Int, height: Int) {
            mediaRecorder.apply { // setters order is important!
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)

                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)

                setAudioEncodingBitRate(16)
                setAudioSamplingRate(44_100)
                setVideoEncodingBitRate(6_000_000)
                setVideoFrameRate(30) // use also setCaptureRate() for time lapse
                setVideoSize(width, height)

                setOrientationHint((360 - (degree.value ?: 0)) % 360)
                setOutputFile(outputFile.fileDescriptor)

                prepare()
                start()
            }
            surface = mediaRecorder.surface
        }
    }
}