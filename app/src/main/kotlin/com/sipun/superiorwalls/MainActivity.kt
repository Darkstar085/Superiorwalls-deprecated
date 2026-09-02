package com.sipun.superiorwalls

import com.sipun.superiorwalls.library.ui.activities.SuperiorwallsActivity

class MainActivity : SuperiorwallsActivity() {
    override fun defaultTheme(): Int = R.style.MyApp_Default
    override fun amoledTheme(): Int = R.style.MyApp_Default_Amoled
    override fun defaultMaterialYouTheme(): Int = R.style.MyApp_Default_MaterialYou
    override fun amoledMaterialYouTheme(): Int = R.style.MyApp_Default_Amoled_MaterialYou
}
