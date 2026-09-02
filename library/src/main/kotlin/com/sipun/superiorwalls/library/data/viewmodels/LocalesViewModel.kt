package com.sipun.superiorwalls.library.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sipun.superiorwalls.library.R
import com.sipun.superiorwalls.library.extensions.context.withXml
import com.sipun.superiorwalls.library.extensions.resources.nextOrNull
import com.sipun.superiorwalls.library.extensions.utils.context
import com.sipun.superiorwalls.library.extensions.utils.lazyMutableLiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

data class ReadableLocale(val tag: String, val name: String)

class LocalesViewModel(application: Application) : AndroidViewModel(application) {

    private val localesData: MutableLiveData<List<ReadableLocale>> by lazyMutableLiveData()
    val locales: List<ReadableLocale>
        get() = localesData.value.orEmpty()

    private suspend fun internalLoadAppLocales() {
        withContext(IO) {
            val availableLocales = ArrayList<ReadableLocale>()
            context.withXml(R.xml.locales_config) { parser ->
                var eventType: Int? = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.name == "locale") {
                            try {
                                val tag = parser.getAttributeValue(0)
                                availableLocales.add(
                                    ReadableLocale(
                                        tag,
                                        Locale.forLanguageTag(tag)
                                            .getDisplayName(Locale.forLanguageTag(tag))
                                    )
                                )
                            } catch (_: Exception) {
                            }
                        }
                    }
                    eventType = parser.nextOrNull()
                }
            }
            localesData.postValue(availableLocales)
        }
    }

    fun loadAppLocales() {
        viewModelScope.launch { internalLoadAppLocales() }
    }
}
