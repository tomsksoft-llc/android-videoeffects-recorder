package com.tomsksoft.videoeffectsrecorder.data

import android.app.Activity
import com.effectssdk.tsvb.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraStore

class CameraStoreImpl(context: Activity): CameraStore<CameraImpl> {
    override val availableCameras =
        arrayOf(CameraImpl(context, Camera.BACK), CameraImpl(context, Camera.FRONT))
}