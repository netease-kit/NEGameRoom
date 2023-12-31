/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.netease.yunxin.app.party"
        minSdk = 21
        targetSdk = 31
        versionCode = 170
        versionName = "1.7.0"
        multiDexEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }

    lint {
        disable += "IconDensities"
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }
}


dependencies {

    implementation("com.google.android.material:material:1.5.0")
    implementation("com.netease.yunxin.kit:alog:1.1.0")
    implementation("com.netease.yunxin.kit.common:common-ui:1.3.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.netease.yunxin.kit.copyrightedmedia:copyrightedmedia:1.7.0")
    implementation("com.netease.yunxin.kit.room:roomkit:1.22.1")
    api(project(":voiceroomkit"))

    // jetpack dependencies
    implementation("androidx.appcompat:appcompat:1.4.2")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    api(project(":game-ui"))
    api(project(":gamekit:gamekit"))

}