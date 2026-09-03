@file:Suppress("unused")

object Libs {
    private const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    private const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    private const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    private const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    private const val appcompatResources = "androidx.appcompat:appcompat-resources:${Versions.appcompat}"
    private const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
    private const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    private const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    private const val palette = "androidx.palette:palette:${Versions.palette}"
    private const val preference = "androidx.preference:preference:${Versions.preference}"
    private const val materialComponents = "com.google.android.material:material:${Versions.materialComponents}"
    private const val activityKtx = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    private const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"

    private const val composeBom = "androidx.compose:compose-bom:${Versions.composeBom}"
    private const val composeUi = "androidx.compose.ui:ui"
    private const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    private const val composeMaterial3 = "androidx.compose.material3:material3"
    private const val composeMaterialIcons = "androidx.compose.material:material-icons-extended"
    private const val composeRuntimeLiveData = "androidx.compose.runtime:runtime-livedata"
    private const val composeActivity = "androidx.activity:activity-compose:${Versions.activityCompose}"
    private const val composeLifecycle = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifecycleCompose}"
    private const val coilCompose = "io.coil-kt:coil-compose:${Versions.coil}"

    private const val lifecycle = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExt}"
    private const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    private const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    private const val room = "androidx.room:room-ktx:${Versions.room}"
    private const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    private const val work = "androidx.work:work-runtime-ktx:${Versions.work}"

    private const val gson = "com.google.code.gson:gson:${Versions.gson}"
    private const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    private const val retrofitGsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    private const val retrofitScalarsConverter = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"

    private const val coil = "io.coil-kt:coil:${Versions.coil}"
    private const val touchImageView = "com.github.MikeOrtiz:TouchImageView:${Versions.touchImageView}"
    private const val harmonicColors = "com.github.LeonardoSM04:HarmonicColorExtractor:${Versions.harmonicColors}"
    private const val sectionedRecyclerView = "com.jahirfiquitiva:sectioned-recyclerview:${Versions.sectionedRecyclerView}@aar"
    private const val fastScrollRecyclerView = "com.github.jahirfiquitiva:RecyclerView-FastScroll:${Versions.fastScrollRecyclerView}"
    private const val permissions = "com.github.fondesa:kpermissions:${Versions.permissions}"
    private const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val muzei = "com.google.android.apps.muzei:muzei-api:${Versions.muzei}"

    val dependencies = arrayOf(
        kotlin, coroutines, coroutinesAndroid,
        appcompat, appcompatResources, cardView, recyclerView, swipeRefreshLayout,
        palette, preference, materialComponents, activityKtx, fragmentKtx,
        composeBom, composeUi, composeUiToolingPreview, composeMaterial3,
        composeMaterialIcons, composeRuntimeLiveData, composeActivity, composeLifecycle, coilCompose,
        lifecycle, livedataKtx, viewmodelKtx, room, work,
        gson, retrofit, retrofitGsonConverter, retrofitScalarsConverter,
        coil, touchImageView, harmonicColors, sectionedRecyclerView,
        fastScrollRecyclerView, permissions, multidex, muzei
    )

    val kspDependencies = arrayOf(roomCompiler)
}
