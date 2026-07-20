package com.srpski.app

import android.app.Application
import android.content.Context
import com.srpski.app.data.ContentRepository
import com.srpski.app.data.ProgressRepository
import com.srpski.app.data.db.AppDatabase
import com.srpski.app.platform.Tts

class SrpskiApp : Application() {

    lateinit var content: ContentRepository
        private set
    lateinit var progress: ProgressRepository
        private set
    lateinit var tts: Tts
        private set

    override fun onCreate() {
        super.onCreate()
        content = ContentRepository(this)
        progress = ProgressRepository(AppDatabase.get(this).progressDao())
        tts = Tts(this)
    }

    val prefs get() = getSharedPreferences("settings", Context.MODE_PRIVATE)

    var strictDiacritics: Boolean
        get() = prefs.getBoolean("strict_diacritics", true)
        set(value) { prefs.edit().putBoolean("strict_diacritics", value).apply() }

    override fun onTerminate() {
        tts.shutdown()
        super.onTerminate()
    }
}
