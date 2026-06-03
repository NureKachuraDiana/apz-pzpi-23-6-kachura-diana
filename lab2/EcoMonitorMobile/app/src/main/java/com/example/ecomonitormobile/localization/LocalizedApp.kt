package com.example.ecomonitormobile.localization

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun ProvideAppLocale(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
    val locale = remember(languageCode) { AppLanguage.toLocale(languageCode) }

    val localizedConfiguration = remember(locale, baseContext) {
        Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }
    }

    val localizedContext = remember(localizedConfiguration, baseContext) {
        baseContext.createConfigurationContext(localizedConfiguration)
    }

    if (activityResultRegistryOwner != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration,
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner
        ) {
            content()
        }
    } else {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration
        ) {
            content()
        }
    }
}
