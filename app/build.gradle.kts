plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "ir.ari.mp3cutter"
        minSdk = 21
        targetSdk = 32
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

}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.core:core-splashscreen:1.0.0-rc01")
    implementation("androidx.appcompat:appcompat:1.6.0-alpha05")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.4.0")
    implementation("com.google.android.material:material:1.6.1")
}