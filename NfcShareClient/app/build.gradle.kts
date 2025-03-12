plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "nfc.share.nfcshare"
    compileSdk = 34

    defaultConfig {
        applicationId = "nfc.share.nfcshare"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("cn.hutool:hutool-core:5.8.16")
    implementation("org.projectlombok:lombok:1.18.32")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("io.github.mayzs:paho.mqtt.android:1.1.7")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}