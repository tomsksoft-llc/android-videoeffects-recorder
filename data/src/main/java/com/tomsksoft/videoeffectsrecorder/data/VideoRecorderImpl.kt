package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Rect
import android.media.MediaRecorder
import android.os.Build
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.io.File

class VideoRecorderImpl(private val context: Context): VideoRecorder<Frame> {
    companion object {
        const val FPS = 30
    }

    override val frame: Subject<Frame> = BehaviorSubject.create()

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
        private var disposable: Disposable? = null

        init {
            disposable = frame.observeOn(Schedulers.io()).subscribe { frame ->
                val (width, height) = frame.bitmap.width to frame.bitmap.height
                if (surface == null) // first frame setups MediaRecorder with appropriate video size
                    start(width, height)
                val canvas = surface!!.lockCanvas(
                    Rect(0, 0, width, height)
                ) ?: return@subscribe
                canvas.drawBitmap(frame.bitmap, 0f, 0f, null)
                surface!!.unlockCanvasAndPost(canvas)
            }
        }

        override fun close() {
            disposable?.dispose()
            mediaRecorder.stop()
            mediaRecorder.release()
        }

        private fun start(width: Int, height: Int) {
            mediaRecorder.apply { // setters order is important!
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)

                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)

                setVideoFrameRate(FPS) // use also setCaptureRate() for time lapse
                setVideoSize(width, height)
                setOutputFile(outputFile)

                prepare()
                start()
            }
            surface = mediaRecorder.surface
        }
    }
}