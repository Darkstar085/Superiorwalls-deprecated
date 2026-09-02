package com.sipun.superiorwalls.library.data.models

import com.android.billingclient.api.ProductDetails
import com.sipun.superiorwalls.library.extensions.utils.price

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class CleanProductDetails(val originalDetails: ProductDetails) {

    val cleanTitle: String
        get() = originalDetails.title.substring(0, originalDetails.title.lastIndexOf("(")).trim()

    override fun toString(): String = "$cleanTitle - ${originalDetails.price}"
}

@Deprecated(
    "Use 'CleanProductDetails' instead",
    ReplaceWith("CleanProductDetails"),
    DeprecationLevel.ERROR
)
typealias CleanSkuDetails = CleanProductDetails
