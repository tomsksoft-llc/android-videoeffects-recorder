package com.tomsksoft.videoeffectsrecorder.data

import android.graphics.Bitmap

object FrameMapper {

    fun toAny(bitmap: Bitmap): Any = bitmap

    fun fromAny(any: Any): Bitmap = any as Bitmap

    /* // Frames marshaling leads to ugly latency
    fun toBytes(bitmap: Bitmap): ByteArray = ByteArrayOutputStream(bitmap.byteCount).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }

    fun fromBytes(byteArray: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)*/
}