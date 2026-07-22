package com.srpski.app

import android.app.Application
import android.content.Context
import com.srpski.app.data.ContentRepository
import com.srpski.app.data.ContentSync
import com.srpski.app.data.InterestRepository
import com.srpski.app.data.ProgressRepository
import com.srpski.app.data.SharedMediaRepository
import com.srpski.app.data.UserPrefs
import com.srpski.app.data.db.AppDatabase
import com.srpski.app.data.media.ArticleFetcher
import com.srpski.app.data.media.TopicClassifier
import com.srpski.app.data.media.YouTubeApi
import com.srpski.app.data.model.TopicMap
import com.srpski.app.platform.Tts
import com.srpski.app.work.PackCheckWorker
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class SrpskiApp : Application() {

    lateinit var content: ContentRepository
        private set
    lateinit var progress: ProgressRepository
        private set
    lateinit var tts: Tts
        private set
    lateinit var httpClient: OkHttpClient
        private set
    lateinit var topicMap: TopicMap
        private set
    lateinit var topicClassifier: TopicClassifier
        private set
    lateinit var interests: InterestRepository
        private set
    lateinit var sharedMedia: SharedMediaRepository
        private set
    lateinit var userPrefs: UserPrefs
        private set
    lateinit var contentSync: ContentSync
        private set
    var youTubeApi: YouTubeApi? = null
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)

        content = ContentRepository(this)
        progress = ProgressRepository(db.progressDao())
        tts = Tts(this)

        httpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        topicMap = loadTopicMap()
        topicClassifier = TopicClassifier(topicMap)

        val apiKey = BuildConfig.YOUTUBE_API_KEY
        if (apiKey.isNotBlank()) {
            youTubeApi = YouTubeApi(httpClient, apiKey)
        }

        userPrefs = UserPrefs(this)
        contentSync = ContentSync(
            http = httpClient,
            dao = db.contentPackDao(),
            content = content,
            packsDir = File(filesDir, "packs"),
            indexUrl = BuildConfig.CONTENT_PACK_INDEX_URL
        )
        interests = InterestRepository(db.interestDao())
        val articleFetcher = ArticleFetcher(httpClient)
        sharedMedia = SharedMediaRepository(
            dao = db.sharedMediaDao(),
            youTubeApi = youTubeApi,
            articleFetcher = articleFetcher,
            topicClassifier = topicClassifier,
            interests = interests,
            mediaDir = File(filesDir, "media")
        )

        PackCheckWorker.schedule(this)
    }

    private fun loadTopicMap(): TopicMap {
        val text = assets.open("topics/topic_map.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        return Json { ignoreUnknownKeys = true }.decodeFromString(TopicMap.serializer(), text)
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
