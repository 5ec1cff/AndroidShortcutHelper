plugins {
    id("com.android.library")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("org.jetbrains:annotations:15.0")
    val hiddenApiRefineVersion: String by project
    annotationProcessor("dev.rikka.tools.refine:annotation-processor:$hiddenApiRefineVersion")
    compileOnly("dev.rikka.tools.refine:annotation:$hiddenApiRefineVersion")
}