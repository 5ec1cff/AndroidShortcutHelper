pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    val hiddenApiRefineVersion: String by settings

    plugins {
        id("com.android.application") version "7.2.2"
        id("com.android.library") version "7.2.2"
        id("org.jetbrains.kotlin.android") version "1.7.10"
        id("dev.rikka.tools.refine") version hiddenApiRefineVersion
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
rootProject.name = "Shortcut"
include(":app")
include(":hidden-api")
