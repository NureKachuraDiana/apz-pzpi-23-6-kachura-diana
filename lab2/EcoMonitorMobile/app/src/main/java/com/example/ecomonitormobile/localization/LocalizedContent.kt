package com.example.ecomonitormobile.localization

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Ensures correct app locale inside dialogs / bottom sheets that may not inherit [ProvideAppLocale].
 */
@Composable
fun LocalizedContent(content: @Composable () -> Unit) {
    val appContext = LocalContext.current.applicationContext
    val languageCode = LocalePreferences(appContext).get()
    ProvideAppLocale(languageCode = languageCode, content = content)
}
