package com.tomsksoft.videoeffectsrecorder

import android.app.Application
import android.app.UiModeManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.tomsksoft.videoeffectsrecorder.domain.Effects

class VideoEffectsRecorderApplication : Application() {


	override fun onCreate() {
		super.onCreate()
		Effects.initialize(applicationContext)
		forceLightTheme()
	}

	private fun forceLightTheme() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			getSystemService(UiModeManager::class.java)
				.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
		else
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
	}
}
