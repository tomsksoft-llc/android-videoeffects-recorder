pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
		flatDir {
			dirs("libs")
		}
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "Video Effects Recorder"
include(":app")
include(":domain")
include(":data")
