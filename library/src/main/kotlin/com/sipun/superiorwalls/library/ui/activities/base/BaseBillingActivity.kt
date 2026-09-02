package com.sipun.superiorwalls.library.ui.activities.base

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import com.android.billingclient.api.ProductDetails
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.data.Preferences
import com.sipun.superiorwalls.library.data.listeners.BillingProcessesListener
import com.sipun.superiorwalls.library.data.models.CleanProductDetails
import com.sipun.superiorwalls.library.data.models.DetailedPurchaseRecord
import com.sipun.superiorwalls.library.data.viewmodels.BillingViewModel
import com.sipun.superiorwalls.library.extensions.context.firstInstallTime
import com.sipun.superiorwalls.library.extensions.context.getAppName
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.context.stringArray
import com.sipun.superiorwalls.library.extensions.fragments.mdDialog
import com.sipun.superiorwalls.library.extensions.fragments.message
import com.sipun.superiorwalls.library.extensions.fragments.negativeButton
import com.sipun.superiorwalls.library.extensions.fragments.positiveButton
import com.sipun.superiorwalls.library.extensions.fragments.singleChoiceItems
import com.sipun.superiorwalls.library.extensions.fragments.title
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.utils.lazyViewModel
import com.sipun.superiorwalls.library.ui.fragments.viewer.IndeterminateProgressDialog

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseBillingActivity<out P : Preferences> : BaseChangelogDialogActivity<P>(),
    BillingProcessesListener {

    val billingViewModel: BillingViewModel by lazyViewModel()
    val isBillingClientReady: Boolean
        get() = billingEnabled && billingViewModel.isBillingClientReady

    private val billingLoadingDialog: IndeterminateProgressDialog by lazy { IndeterminateProgressDialog.create() }
    private var purchasesDialog: AlertDialog? = null

    open val billingEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (billingEnabled) {
            billingViewModel.billingProcessesListener = this
            billingViewModel.observe(this)
            billingViewModel.initialize()
        }
    }

    override fun onResume() {
        super.onResume()
        if (preferences.isFirstRun && firstInstallTime > 10000) {
            preferences.isFirstRun = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val created = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.donate)?.isVisible =
            isBillingClientReady && getDonationItemsIds().isNotEmpty()
        return created
    }

    private fun dismissDialogs() {
        try {
            billingLoadingDialog.dismiss()
        } catch (e: Exception) {
        }
        try {
            purchasesDialog?.dismiss()
        } catch (e: Exception) {
        }
        purchasesDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialogs()
        billingViewModel.destroy(this)
    }

    fun showDonationsDialog() {
        if (!isBillingClientReady) {
            onProductPurchaseError()
            return
        }
        val productDetailsList =
            billingViewModel.inAppProductDetails.map { CleanProductDetails(it) }
                .filter { getDonationItemsIds().contains(it.originalDetails.productId) }
        if (productDetailsList.isEmpty()) {
            onProductPurchaseError()
            return
        }
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.donate)
            singleChoiceItems(productDetailsList, 0)
            negativeButton(android.R.string.cancel)
            positiveButton(R.string.donate) { dialog ->
                val listView = (dialog as? AlertDialog)?.listView
                if ((listView?.checkedItemCount ?: 0) > 0) {
                    val checkedItemPosition = listView?.checkedItemPosition ?: -1
                    billingViewModel.launchBillingFlow(
                        this@BaseBillingActivity,
                        productDetailsList[checkedItemPosition].originalDetails
                    )
                }
                dialog.dismiss()
            }
        }
        purchasesDialog?.show()
    }

    override fun onProductPurchaseSuccess(purchase: DetailedPurchaseRecord?) {
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.donate_success_title)
            message(string(R.string.donate_success_content, getAppName()))
            positiveButton(android.R.string.ok)
        }
        purchasesDialog?.show()
    }

    override fun onProductPurchaseError(purchase: DetailedPurchaseRecord?) {
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.error)
            message(string(R.string.unexpected_error_occurred))
        }
        purchasesDialog?.show()
    }

    override fun onBillingClientReady() {
        super.onBillingClientReady()
        invalidateOptionsMenu()
        val inAppItems =
            ArrayList(getDonationItemsIds()).apply { addAll(getInAppPurchasesItemsIds()) }
        billingViewModel.queryInAppProductDetailsList(inAppItems)
        billingViewModel.querySubscriptionsProductDetailsList(getSubscriptionsItemsIds())
    }

    override fun onInAppProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {
        super.onInAppProductDetailsListUpdated(productDetailsList)
        invalidateOptionsMenu()
    }

    override fun onSubscriptionsProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {
        super.onSubscriptionsProductDetailsListUpdated(productDetailsList)
        invalidateOptionsMenu()
    }

    override fun onBillingClientDisconnected() {
        super.onBillingClientDisconnected()
        invalidateOptionsMenu()
    }

    open fun getDonationItemsIds(): List<String> = try {
        stringArray(R.array.donation_items).filter { it.hasContent() }
    } catch (e: Exception) {
        listOf()
    }

    open fun getInAppPurchasesItemsIds(): List<String> = listOf()
    open fun getSubscriptionsItemsIds(): List<String> = listOf()
}
