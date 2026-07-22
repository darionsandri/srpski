package com.srpski.app.data.media

import com.srpski.app.data.db.SharedMedium
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UrlClassifierTest {

    @Test fun bareYoutubeWatchUrlExtractsId() {
        val c = UrlClassifier.classify("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertEquals(SharedMedium.KIND_YOUTUBE, c.kind)
        assertEquals("dQw4w9WgXcQ", c.youtubeId)
    }

    @Test fun shareSheetTextWithLeadingCopyPrefixStillExtractsId() {
        val c = UrlClassifier.classify("Check this out https://youtu.be/dQw4w9WgXcQ cool right?")
        assertEquals(SharedMedium.KIND_YOUTUBE, c.kind)
        assertEquals("dQw4w9WgXcQ", c.youtubeId)
    }

    @Test fun mobileYoutubeSubdomain() {
        val c = UrlClassifier.classify("https://m.youtube.com/watch?v=abc12345_-x")
        assertEquals(SharedMedium.KIND_YOUTUBE, c.kind)
        assertEquals("abc12345_-x", c.youtubeId)
    }

    @Test fun shortsUrl() {
        val c = UrlClassifier.classify("https://youtube.com/shorts/dQw4w9WgXcQ")
        assertEquals(SharedMedium.KIND_YOUTUBE, c.kind)
        assertEquals("dQw4w9WgXcQ", c.youtubeId)
    }

    @Test fun embedUrl() {
        val c = UrlClassifier.classify("https://www.youtube.com/embed/dQw4w9WgXcQ")
        assertEquals(SharedMedium.KIND_YOUTUBE, c.kind)
        assertEquals("dQw4w9WgXcQ", c.youtubeId)
    }

    @Test fun malformedYoutubeIdRejected() {
        val c = UrlClassifier.classify("https://www.youtube.com/watch?v=tooShort")
        assertEquals(SharedMedium.KIND_ARTICLE, c.kind)
        assertNull(c.youtubeId)
    }

    @Test fun nonYoutubeUrlIsArticle() {
        val c = UrlClassifier.classify("https://example.com/some-article")
        assertEquals(SharedMedium.KIND_ARTICLE, c.kind)
        assertEquals("https://example.com/some-article", c.url)
    }

    @Test fun plainTextIsText() {
        val c = UrlClassifier.classify("just some text I copied")
        assertEquals(SharedMedium.KIND_TEXT, c.kind)
        assertNull(c.url)
    }

    @Test fun firstUrlPicksTheFirstHttpToken() {
        val extracted = UrlClassifier.firstUrl("see https://a.com and https://b.com")
        assertNotNull(extracted)
        assertEquals("https://a.com", extracted)
    }
}
