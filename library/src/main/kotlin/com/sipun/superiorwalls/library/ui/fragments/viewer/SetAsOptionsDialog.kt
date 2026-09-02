package com.sipun.superiorwalls.library.ui.fragments.viewer

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.extensions.fragments.mdDialog
import com.sipun.superiorwalls.library.extensions.fragments.negativeButton
import com.sipun.superiorwalls.library.extensions.fragments.positiveButton
import com.sipun.superiorwalls.library.extensions.fragments.singleChoiceItems
import com.sipun.superiorwalls.library.extensions.fragments.title
import com.sipun.superiorwalls.library.ui.activities.ViewerActivity

class SetAsOptionsDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return requireContext().mdDialog {
            title(R.string.apply_to)
            singleChoiceItems(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    R.array.set_wallpaper_options
                else R.array.set_wallpaper_options_pre_nougat,
                -1
            )
            positiveButton(android.R.string.ok) {
                val listView = (dialog as? AlertDialog)?.listView
                if ((listView?.checkedItemCount ?: 0) > 0) {
                    val checkedItemPosition = listView?.checkedItemPosition ?: -1
                    val actualOption =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) checkedItemPosition
                        else checkedItemPosition + 2
                    (activity as? ViewerActivity)?.startApply(actualOption)
                }
                dismiss()
            }
            negativeButton(android.R.string.cancel) { dismiss() }
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
        }
    }

    companion object {
        internal const val TAG = "set_wallpaper_options_dialog"
    }
}