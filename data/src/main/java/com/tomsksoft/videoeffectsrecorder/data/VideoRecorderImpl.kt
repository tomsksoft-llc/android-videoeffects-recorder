package com.tomsksoft.videoeffectsrecorder.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.io.File
import java.io.FileDescriptor


@SuppressLint("CheckResult")
class VideoRecorderImpl(private val context: Context): VideoRecorder<Frame> {

    override val frame: Subject<Frame> = BehaviorSubject.create()
    override val degree: Subject<Int> = BehaviorSubject.create()

    @Volatile
    private var cachedDegree: Int? = null

    init {
        degree.observeOn(Schedulers.io()).subscribe {
            cachedDegree = it
        }
    }

    override fun startRecord(outputFile: File): VideoRecorder.Record = RecordImpl(outputFile)

    private inner class RecordImpl(private val outputFile: File): VideoRecorder.Record {

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
                val (width, height) = frame.bitmap.width to frame.bitmap.height
                // first frame setups MediaRecorder with appropriate video size and orientation
                if (surface == null)
                    start(width, height)
                val canvas = surface!!.lockCanvas(
                    Rect(0, 0, width, height)
                ) ?: return@subscribe
                canvas.drawBitmap(frame.bitmap, 0f, 0f, null)
                surface!!.unlockCanvasAndPost(canvas)
            }
        }

        override fun close() {
            disposableFrameSubscription?.dispose()
            mediaRecorder.stop()
            mediaRecorder.release()
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

                setOrientationHint((360 - (cachedDegree ?: 0)) % 360)
                setOutputFile(outputFile)

                prepare()
                start()
            }
            surface = mediaRecorder.surface
        }
    }
}