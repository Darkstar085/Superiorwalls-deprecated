package com.sipun.superiorwalls.library.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.sipun.superiorwalls.library.BuildConfig
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.models.AboutItem
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.context.stringArray
import com.sipun.superiorwalls.library.ui.activities.base.BaseThemedActivity
import com.sipun.superiorwalls.library.ui.compose.AboutScreen
import com.sipun.superiorwalls.library.ui.compose.SuperiorwallsTheme

open class AboutActivity : BaseThemedActivity<Preferences>() {
    override val preferences: Preferences by lazy { Preferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperiorwallsTheme(
                darkTheme = preferences.currentTheme == Preferences.ThemeKey.DARK,
                dynamicColor = preferences.useMaterialYou,
                amoled = preferences.usesAmoledTheme,
            ) {
                AboutScreen(
                    title = dashboardName,
                    designerItems = getDesignerAboutItems(),
                    internalItems = getInternalAboutItems(),
                    onBack = { supportFinishAfterTransition() },
                )
            }
        }
    }

    private fun getDesignerAboutItems(): ArrayList<AboutItem> {
        val names = stringArray(R.array.credits_titles)
        val descriptions = stringArray(R.array.credits_descriptions)
        val photos = stringArray(R.array.credits_photos)
        val buttonTexts = stringArray(R.array.credits_buttons)
        val buttonLinks = stringArray(R.array.credits_links)
        if (listOf(names.size, descriptions.size, photos.size, buttonTexts.size, buttonLinks.size).distinct().size != 1) return arrayListOf()
        return ArrayList(names.indices.map { index ->
            AboutItem(names[index], descriptions[index], photos[index], ArrayList(buttonTexts[index].split("|").zip(buttonLinks[index].split("|"))))
        })
    }

    private fun getInternalAboutItems() = getAdditionalInternalAboutItems().apply {
        add(AboutItem("Jahir Fiquitiva", string(R.string.jahir_description), "https://jahir.dev/static/images/jahir/jahir.jpg", arrayListOf("Website" to "https://jahir.dev", "GitHub" to "https://github.com/jahirfiquitiva")))
        if (shouldIncludeContributors()) {
            add(AboutItem("Eduardo Pratti", string(R.string.eduardo_description), "https://pbs.twimg.com/profile_images/560688750247051264/seXz0Y25_400x400.jpeg", arrayListOf("Website" to "https://pratti.design/")))
            add(AboutItem("Patryk Michalik", string(R.string.patryk_description), "https://raw.githubusercontent.com/patrykmichalik/brand/master/logo-on-indigo.png", arrayListOf("Website" to "https://patrykmichalik.com")))
        }
    }

    open fun getAdditionalInternalAboutItems(): ArrayList<AboutItem> = arrayListOf()
    open fun shouldIncludeContributors(): Boolean = true
    open val dashboardName = BuildConfig.DASHBOARD_NAME
}
