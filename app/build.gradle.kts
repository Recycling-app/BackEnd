plugins {
    id("com.google.gms.google-services")
    id("com.android.application")
}

android {
    namespace = "com.example.myrecycleplication"
    compileSdk = 35
    // 최신 Android SDK 버전으로 맞춰주세요.

    defaultConfig {
        applicationId = "com.example.myrecycleplication"
        minSdk = 24
        targetSdk = 35// 최소 지원 Android 버전
        // 타겟 Android SDK 버전
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Google Play Services Location 라이브러리 (위치 정보 가져오기용)
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // 카카오 벡터 맵 SDK
    implementation("com.kakao.maps.open:android:2.12.8")


    // 공공데이터 api
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter ( JSON 타입 결과를 객체로 매핑 )
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    // 네트워크 통신
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 파베
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-firestore:25.0.0")

    // 기본 AndroidX 라이브러리
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    // 테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    implementation("com.kakao.maps.open:android:2.11.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}