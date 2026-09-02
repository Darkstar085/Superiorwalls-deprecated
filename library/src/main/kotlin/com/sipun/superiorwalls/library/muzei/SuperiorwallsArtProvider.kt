package com.sipun.superiorwalls.library.muzei

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.extensions.context.getAppName
import com.sipun.superiorwalls.library.extensions.context.isNetworkAvailable
import com.sipun.superiorwalls.library.extensions.context.isWifiConnected

open class SuperiorwallsArtProvider : MuzeiArtProvider() {

    open val worker: SuperiorwallsArtWorker by lazy { SuperiorwallsArtWorker() }

    open fun getPreferences(context: Context): Preferences = Preferences(context)

    override fun onLoadRequested(initial: Boolean) {
        val prefs = context?.let { getPreferences(it) }
        prefs ?: return
        if (prefs.functionalDashboard && context?.isNetworkAvailable() == true) {
            if (prefs.refreshMuzeiOnWiFiOnly) {
                if (context?.isWifiConnected == true) worker.loadWallpapers(context, prefs)
            } else {
                worker.loadWallpapers(context, prefs)
            }
        }
    }

    private fun createShareAction(artwork: Artwork) = context?.run {
        val title = getString(R.string.share)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            getString(
                R.string.share_text,
                artwork.title.orEmpty(),
                artwork.byline.orEmpty(),
                getAppName(),
                "https://play.google.com/store/apps/details?id=" + packageName.orEmpty()
            )
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        RemoteActionCompat(
            IconCompat.createWithResource(this, R.drawable.ic_share),
            title, title,
            PendingIntent.getActivity(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        )
    }

    override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
        context ?: return super.getCommandActions(artwork)
        return listOfNotNull(createShareAction(artwork))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        worker.destroy()
    }
}
