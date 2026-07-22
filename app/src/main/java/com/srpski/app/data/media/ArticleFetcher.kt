package com.srpski.app.data.media

import com.srpski.app.data.model.ArticleDoc
import okhttp3.OkHttpClient
import okhttp3.Request

/** Thin OkHttp wrapper that GETs the URL and hands the HTML to [ArticleExtractor]. */
class ArticleFetcher(private val client: OkHttpClient) {

    fun fetch(url: String): ArticleDoc? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "SrpskiApp/1.0 (+educational)")
            .get()
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val html = response.body?.string() ?: return null
                ArticleExtractor.extract(url, html)
            }
        }.getOrNull()
    }
}
