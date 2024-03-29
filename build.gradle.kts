// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-alpha04" apply false
    id("com.android.library") version "8.2.0-alpha04" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}