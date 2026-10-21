package io.github.suriyadi15.qrishook

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate

object AppLocale {
    /**
     * Context whose resources follow the in-app language choice. Needed outside activities
     * (e.g. the foreground service), where AppCompat does not apply the choice before Android 13.
     */
    fun localized(context: Context): Context {
        val chosen = AppCompatDelegate.getApplicationLocales()
        if (chosen.isEmpty) return context

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(chosen.toLanguageTags()))
        return context.createConfigurationContext(configuration)
    }
}
