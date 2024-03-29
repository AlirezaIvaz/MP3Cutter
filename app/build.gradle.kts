import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "ir.ari.mp3cutter"
    compileSdk = 33

    defaultConfig {
        applicationId = "ir.ari.mp3cutter"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    flavorDimensions += "distributor"
    productFlavors {
        create("general") {
            dimension = "distributor"
            buildConfigField("String", "FLAVOR_REVIEW_INTENT", "\"\"")
            buildConfigField("String", "FLAVOR_APPS_INTENT", "\"\"")
        }
        create("googlePlay") {
            dimension = "distributor"
            buildConfigField(
                "String",
                "FLAVOR_REVIEW_INTENT",
                "\"market://details?id=${defaultConfig.applicationId}\""
            )
            buildConfigField("String", "FLAVOR_APPS_INTENT", "\"market://dev?id=0\"")
        }
        create("galaxyStore") {
            dimension = "distributor"
            buildConfigField(
                "String",
                "FLAVOR_REVIEW_INTENT",
                "\"samsungapps://AppRating/${defaultConfig.applicationId}\""
            )
            buildConfigField(
                "String",
                "FLAVOR_APPS_INTENT",
                "\"samsungapps://SellerDetail/000000055766\""
            )
        }
        create("cafeBazaar") {
            dimension = "distributor"
            buildConfigField(
                "String",
                "FLAVOR_REVIEW_INTENT",
                "\"bazaar://details?id=${defaultConfig.applicationId}\""
            )
            buildConfigField(
                "String",
                "FLAVOR_APPS_INTENT",
                "\"bazaar://collection?slug=by_author&aid=alirezaivaz\""
            )
        }
        create("myket") {
            dimension = "distributor"
            buildConfigField(
                "String",
                "FLAVOR_REVIEW_INTENT",
                "\"myket://comment?id=${defaultConfig.applicationId}\""
            )
            buildConfigField(
                "String",
                "FLAVOR_APPS_INTENT",
                "\"myket://developer/${defaultConfig.applicationId}\""
            )
        }
        create("charkhoneh") {
            dimension = "distributor"
            buildConfigField(
                "String",
                "FLAVOR_REVIEW_INTENT",
                "\"jhoobin://comment?q=${defaultConfig.applicationId}\""
            )
            buildConfigField(
                "String",
                "FLAVOR_APPS_INTENT",
                "\"jhoobin://collection?type=APP&id=27501\""
            )
        }
    }

    project.tasks.preBuild.dependsOn("copyLicenseToAssets")

}

tasks.register<Copy>("copyLicenseToAssets") {
    mkdir("$projectDir/src/main/assets")
    from("$rootDir/THANKS.md")
    into("$projectDir/src/main/assets")
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.core:core-splashscreen:1.1.0-alpha01")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha02")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("io.noties.markwon:core:4.6.2")
}