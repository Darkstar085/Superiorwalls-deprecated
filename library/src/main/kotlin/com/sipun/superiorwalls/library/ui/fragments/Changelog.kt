package com.sipun.superiorwalls.library.ui.fragments

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.extensions.context.string
import com.sipun.superiorwalls.library.extensions.context.withXml
import com.sipun.superiorwalls.library.extensions.resources.getAttributeValue
import com.sipun.superiorwalls.library.extensions.resources.hasContent
import com.sipun.superiorwalls.library.extensions.resources.nextOrNull
import org.xmlpull.v1.XmlPullParser

data class ChangelogEntry(
    val text: String,
    val type: ChangelogType,
)

enum class ChangelogType(val tag: String, val attr: String) {
    TITLE("version", "title"),
    ITEM("item", "text");

    companion object {
        val values = values()
    }

    fun add(parser: XmlResourceParser, list: MutableList<ChangelogEntry>): Boolean {
        if (parser.name != tag) return false
        val value = parser.getAttributeValue(attr).orEmpty()
        if (value.hasContent()) list.add(ChangelogEntry(value, this))
        return true
    }
}

fun Context.buildChangelogEntries(@XmlRes xmlRes: Int = R.xml.changelog): List<ChangelogEntry> {
    return try {
        parseChangelog(this, xmlRes)
    } catch (_: Exception) {
        emptyList()
    }
}

fun parseChangelog(context: Context, @XmlRes xmlRes: Int): List<ChangelogEntry> {
    val items = mutableListOf<ChangelogEntry>()
    context.withXml(xmlRes) { parser ->
        var eventType: Int? = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && eventType != null) {
            if (eventType == XmlPullParser.START_TAG) {
                ChangelogType.values.any { it.add(parser, items) }
            }
            eventType = parser.nextOrNull()
        }
    }
    return items
}

fun Context.changelogTitle(@StringRes title: Int = R.string.changelog): String = string(title)
