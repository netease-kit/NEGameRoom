/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // jetpack dependencies
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.1")

    // xkit dependencies
    api(project(":voiceroomkit:voiceroomkit-ui-base"))
    api(project(":voiceroomkit:gamekit:gamekit"))
    implementation("com.netease.yunxin.kit.common:common-ui:1.3.1")
    implementation("com.netease.yunxin.kit:alog:1.1.0")
}
