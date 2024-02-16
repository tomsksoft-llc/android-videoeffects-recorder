package com.tomsksoft.videoeffectsrecorder

import android.app.Application
import android.app.UiModeManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.effectssdk.tsvb.EffectsSDK

class VideoEffectsRecorderApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		EffectsSDK.initialize(applicationContext)
	}
}
