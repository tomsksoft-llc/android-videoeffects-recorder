package com.tomsksoft.videoeffectsrecorder

import android.app.Application
import com.effectssdk.tsvb.EffectsSDK
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.PipelineConfigManager

class VideoEffectsRecorderApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		EffectsSDK.initialize(applicationContext)
	}
}
