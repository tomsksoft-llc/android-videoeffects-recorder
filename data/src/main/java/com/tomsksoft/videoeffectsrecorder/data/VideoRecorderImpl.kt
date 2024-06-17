package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.boundary.VideoRecorder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class VideoRecorderImpl(
    private val context: Context,
    private val directoryName: String
): VideoRecorder {
    companion object {
        private const val TAG = "VideoRecorder"
    }

    private val fileStorageAccessor = FileStorageAccessor(context)

    override fun startRecord(
        frameSource: Observable<Any>,
        orientation: Int,
        filename: String,
        mimeType: String
    ): AutoCloseable {
        val (descriptor, uri) = fileStorageAccessor.createVideoFileDescriptor(
            filename,
            directoryName,
            mimeType
        )
        return Record(frameSource, orientation, descriptor, uri)
    }

    private inner class Record(
        frameSource: Observable<Any>,
        private val orientation: Int,
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
            disposableFrameSubscription = frameSource
                .observeOn(Schedulers.io())
                .subscribe(this::onNewFrame)
        }

        override fun close() {
            Log.d(TAG, "Interrupt video record in state: $recordState")
            when (recordState) {
                RecordState.IDLE -> stop()

                RecordState.STARTING -> recordState = RecordState.STOPPING // stop ASAP

                RecordState.RECORDING -> stop()

                RecordState.STOPPING -> throw IllegalStateException("Already stopping")

                RecordState.STOPPED -> throw IllegalStateException("Already stopped")
            }
        }

        private fun onNewFrame(frame: Any) {
            val bitmap = FrameMapper.fromAny(frame)
            // first frame setups MediaRecorder with appropriate video size and orientation
            when (recordState) {
                RecordState.IDLE -> {
                    start(bitmap.width, bitmap.height) // sets state in STOPPED or RECORDING
                    if (recordState == RecordState.RECORDING) onNewFrame(frame) // don't lose first frame
                }

                RecordState.STARTING, RecordState.STOPPING, RecordState.STOPPED ->
                    Log.w(TAG, "Cancel drawing in state: $recordState") // unreachable

                RecordState.RECORDING -> draw(bitmap)
            }
        }

        private fun draw(bitmap: Bitmap) {
            val canvas = surface!!.lockCanvas(
                Rect(0, 0, bitmap.width, bitmap.height)
            ) ?: return
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            surface!!.unlockCanvasAndPost(canvas)
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

                setOrientationHint(orientation)
                setOutputFile(parcelDescriptor.fileDescriptor)

                prepare()
            }
            surface = mediaRecorder.surface

            if (recordState == RecordState.STOPPING) {
                Log.d(TAG, "Stop was requested while setting MediaRecorder")
                stop()
                return
            }

            mediaRecorder.start() // long operation

            if (recordState == RecordState.STOPPING) {
                Log.d(TAG, "Stop was requested while starting MediaRecorder")
                stop()
                return
            }

            recordState = RecordState.RECORDING
        }

        private fun stop() {
            recordState = RecordState.STOPPED
            disposableFrameSubscription?.dispose()

            try {
                mediaRecorder.stop()
            } catch (e: RuntimeException) { // no valid audio/video data has been received
                fileStorageAccessor.deleteFile(file)
            }

            mediaRecorder.release()
            parcelDescriptor.close()
        }
    }

    private enum class RecordState {
        IDLE, // waiting for first frame to setup MediaRecorder
        STARTING, // setting up MediaRecorder
        RECORDING, // started
        STOPPING, // request stop while starting MediaRecorder
        STOPPED
    }
}