package com.example.ecomonitormobile.localization

import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Resolves strings using the app language from [LocalePreferences].
 * Use in [androidx.compose.material3.ModalBottomSheet] and other overlays that
 * may not inherit [ProvideAppLocale]'s [androidx.compose.ui.platform.LocalContext].
 */
@Composable
fun localizedStringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val applicationContext = LocalContext.current.applicationContext
    val languageCode = LocalePreferences(applicationContext).get()
    val localizedContext = remember(languageCode) {
        val locale: Locale = AppLanguage.toLocale(languageCode)
        val configuration = Configuration(applicationContext.resources.configuration).apply {
            setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setLocales(LocaleList(locale))
            }
        }
        applicationContext.createConfigurationContext(configuration)
    }
    return if (formatArgs.isEmpty()) {
        localizedContext.getString(id)
    } else {
        localizedContext.getString(id, *formatArgs)
    }
}
