package com.tomsksoft.videoeffectsrecorder.domain

import android.content.Context
import com.effectssdk.tsvb.EffectsSDK

class Effects {
    companion object {
        fun initialize(context: Context) = EffectsSDK.initialize(context)
    }
}