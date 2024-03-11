package com.tomsksoft.videoeffectsrecorder

import android.app.Application
import com.effectssdk.tsvb.EffectsSDK

class VideoEffectsRecorderApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		EffectsSDK.initialize(applicationContext)
	}
}
