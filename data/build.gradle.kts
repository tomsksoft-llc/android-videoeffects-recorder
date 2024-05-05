plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
}

android {
	namespace = "com.tomsksoft.videoeffectsrecorder.data"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.midSdk.get().toInt()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation(project(mapOf("path" to ":domain")))
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle)

	/* RxJava */
	implementation(libs.rxjava3.rxjava)

	/* Effects SDK */
	implementation(libs.flogger.flogger)
	implementation(libs.flogger.system.backend)
	implementation(libs.guava)
	implementation(files("../libs/effects.aar"))
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.extensions)
	implementation(libs.androidx.camera.lifecycle)
	implementation(libs.androidx.camera.view)
}