package com.tomsksoft.videoeffectsrecorder.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class VideoRecorderImpl(
    private val context: Context,
    private val directoryName: String
): VideoRecorder {
    companion object {
        private const val TAG = "VideoRecorder"
    }

    override val frame: BehaviorSubject<Any> = BehaviorSubject.create()
    override val degree: BehaviorSubject<Int> = BehaviorSubject.create()

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override fun startRecord(filename: String, mimeType: String): AutoCloseable {
        val (descriptor, uri) = createFile(filename, mimeType)
        return Record(descriptor, uri)
    }

    @SuppressLint("Recycle") // ParcelFileDescriptor will be freed by Record
    private fun createFile(filename: String, mimeType: String): Pair<ParcelFileDescriptor, Uri> {
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$directoryName")
        })!! // MediaStore concerns about making dirs and providing unique filenames on its own
        return contentResolver.openFileDescriptor(uri, "w")!! to uri
    }

    private fun deleteFile(file: Uri) {
        contentResolver.delete(file, null, null)
    }

    private inner class Record(
        private val parcelDescriptor: ParcelFileDescriptor,
        private val file: Uri
    ): AutoCloseable {
        @Volatile
        private var recordState: RecordState = RecordState.IDLE
        private val mediaRecorder = (
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                    MediaRecorder()
                else
                    MediaRecorder(context)
            )
        private var surface: Surface? = null
        private var disposableFrameSubscription: Disposable? = null

        init {
            disposableFrameSubscription = frame.observeOn(Schedulers.io()).subscribe { frame ->
                val bitmap = FrameMapper.fromAny(frame)
                val (width, height) = bitmap.width to bitmap.height
                // first frame setups MediaRecorder with appropriate video size and orientation
                if (surface == null)
                    start(width, height)
                if (recordState != RecordState.RECORDING)
                    return@subscribe
                val canvas = surface!!.lockCanvas(
                    Rect(0, 0, width, height)
                ) ?: return@subscribe
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                surface!!.unlockCanvasAndPost(canvas)
            }
        }

        override fun close() {
            Log.d(TAG, "Interrupt video record in state: $recordState")
            when (recordState) {
                RecordState.IDLE -> {
                    recordState = RecordState.STOPPED
                    disposableFrameSubscription?.dispose()
                    mediaRecorder.release()
                    parcelDescriptor.close()
                }

                RecordState.STARTING -> // start() will check if STOPPING was requested
                    recordState = RecordState.STOPPING

                RecordState.RECORDING -> {
                    recordState = RecordState.STOPPED
                    disposableFrameSubscription?.dispose()
                    mediaRecorder.stop()
                    mediaRecorder.release()
                    parcelDescriptor.close()
                }

                RecordState.STOPPING -> { // MediaRecorder wasn't started, so there's no frames recorded
                    recordState = RecordState.STOPPED
                    disposableFrameSubscription?.dispose()
                    mediaRecorder.release()
                    parcelDescriptor.close()
                    deleteFile(file) // video without frames would represent broken file
                }

                RecordState.STOPPED ->
                    throw IllegalStateException("Already stopped")
            }
        }

        private fun start(width: Int, height: Int) {
            recordState = RecordState.STARTING

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
                setOutputFile(parcelDescriptor.fileDescriptor)

                prepare()
            }
            surface = mediaRecorder.surface

            if (recordState == RecordState.STOPPING) // cancel if stop is already requested
                close()
            else {
                recordState = RecordState.RECORDING
                mediaRecorder.start()
            }
        }
    }

    private enum class RecordState {
        IDLE, // waiting for first frame to setup MediaRecorder
        STARTING, // setting up MediaRecorder
        RECORDING, // MediaRecorder catches frames
        STOPPING, // waiting for finish setting up MediaRecorder to release after
        STOPPED
    }
}