plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.kapt)
	alias(libs.plugins.hilt.android)
}

android {
	namespace = "com.tomsksoft.videoeffectsrecorder"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.tomsksoft.videoeffectsrecorder"
		minSdk = libs.versions.midSdk.get().toInt()
		targetSdk = libs.versions.targetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables {
			useSupportLibrary = true
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			buildConfigField("String", "RECORDS_DIRECTORY", "\"Effects\"")
		}
		debug {
			buildConfigField("String", "RECORDS_DIRECTORY", "\"Effects\"")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {

	implementation(project(mapOf("path" to ":domain")))
	implementation(project(mapOf("path" to ":data")))

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.lifecycle.runtimeCompose)
	implementation(libs.androidx.lifecycle.viewModelCompose)
	implementation(libs.androidx.activity.compose)

	implementation(platform(libs.androidx.compose.bom))

	implementation(libs.androidx.compose.ui.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3.android)
	implementation(libs.androidx.navigation.compose)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.test.ext)
	androidTestImplementation(libs.androidx.test.espresso)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.testManifest)

	implementation(libs.accompanist.permissions)

	/* RxAndroid */
	implementation(libs.rxjava3.rxandroid)
	implementation(libs.rxjava3.rxjava)
	implementation(libs.androidx.compose.runtime.rxjava3)

	/* Effects SDK */
	implementation(project(":effects"))
	implementation(libs.flogger.flogger)
	implementation(libs.flogger.system.backend)
	implementation(libs.guava)
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.extensions)
	implementation(libs.androidx.camera.lifecycle)
	implementation(libs.androidx.camera.view)

	/* Hilt */
	kapt(libs.hilt.android.compiler)
	implementation(libs.hilt.android)
	implementation(libs.hilt.navigation.compose)
}