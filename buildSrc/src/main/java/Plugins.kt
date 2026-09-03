@file:Suppress("unused")

object Plugins {
    // Android
    const val android = "com.android.tools.build:gradle:${Versions.gradle}"

    // Kotlin
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val compose =
        "org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${Versions.kotlin}"

    // KSP
    const val ksp =
        "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${Versions.ksp}"
}
