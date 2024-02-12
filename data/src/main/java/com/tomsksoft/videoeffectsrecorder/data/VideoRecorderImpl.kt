package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Rect
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import java.io.File

class VideoRecorderImpl(private val context: Context): VideoRecorder<Frame> {

    private var record: VideoRecorder.Record<Frame>? = null

    override fun onFrame(frame: Frame) {
        record?.onFrame(frame)
    }

    override fun startRecord(): VideoRecorder.Record<Frame> =
        RecordImpl(context).also { record = it }

    private class RecordImpl(context: Context): VideoRecorder.Record<Frame> {
        companion object {
            const val FPS = 30
            val DEBUG_FILE = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "effects.mp4" // TODO [tva] get free name for file
            )
        }

        private val mediaRecorder = (
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                    MediaRecorder()
                else
                    MediaRecorder(context)
            )
        private var surface: Surface? = null

        override fun onFrame(frame: Frame) {
            val (width, height) = frame.bitmap.width to frame.bitmap.height
            if (surface == null) // first frame setups MediaRecorder with appropriate video size
                start(width, height)
            val canvas = surface!!.lockCanvas(
                Rect(0, 0, width, height)
            ) ?: return
            canvas.drawBitmap(frame.bitmap, 0f, 0f, null)
            surface!!.unlockCanvasAndPost(canvas)
        }

        override fun close() {
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

                setVideoFrameRate(FPS) // is it different from setCaptureRate()?
                setVideoSize(width, height)
                setOutputFile(DEBUG_FILE)

                prepare()
                start()
            }
            surface = mediaRecorder.surface
        }
    }
}