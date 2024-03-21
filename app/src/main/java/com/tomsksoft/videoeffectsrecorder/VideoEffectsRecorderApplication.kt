package com.tomsksoft.videoeffectsrecorder

import android.app.Application
import com.effectssdk.tsvb.EffectsSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VideoEffectsRecorderApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		EffectsSDK.initialize(applicationContext)
	}
}
