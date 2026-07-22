package com.srpski.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * DataStore-backed preferences. Replaces the legacy SharedPreferences("settings")
 * for anything new (onboarding, sync cadence). `strictDiacritics` stays on the
 * legacy store for now to avoid surprising existing users; see SrpskiApp.
 */
class UserPrefs(private val context: Context) {

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }
    val autoCheckPacks: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CHECK] ?: true }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    suspend fun setAutoCheckPacks(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CHECK] = enabled }
    }

    companion object {
        private val KEY_ONBOARDING_DONE: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_done")
        private val KEY_AUTO_CHECK: Preferences.Key<Boolean> = booleanPreferencesKey("auto_check_packs")
    }
}
