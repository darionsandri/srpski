package com.srpski.app.data.media

import com.srpski.app.data.model.ArticleDoc
import com.srpski.app.data.model.TopicDef
import com.srpski.app.data.model.TopicMap
import com.srpski.app.data.model.YouTubeMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicClassifierTest {

    private val map = TopicMap(
        schemaVersion = 1,
        topics = listOf(
            TopicDef("music", "Music", listOf("music", "album", "song"), listOf(10)),
            TopicDef("food", "Food", listOf("recipe", "cooking"), listOf(26)),
            TopicDef("sport", "Sport", listOf("football", "match"), listOf(17))
        )
    )
    private val subject = TopicClassifier(map)

    @Test fun youtubeCategoryOutranksSingleKeywordHit() {
        val meta = YouTubeMeta(
            videoId = "abc",
            title = "This mentions football and recipe",
            categoryId = 10,       // -> music
            tags = emptyList()
        )
        val ranking = subject.classifyYouTube(meta)
        assertEquals("music", ranking.first())
        // sport + food still tie for keyword hit, both present but after music
        assertTrue("expected sport and food both to appear, got $ranking",
            "sport" in ranking && "food" in ranking)
    }

    @Test fun youtubeTagsCountMoreThanTitleKeyword() {
        val meta = YouTubeMeta(
            videoId = "abc",
            title = "generic clip",
            categoryId = null,
            tags = listOf("football highlights", "premier league match")
        )
        val ranking = subject.classifyYouTube(meta)
        assertEquals("sport", ranking.first())
    }

    @Test fun articleClassifierUsesCorpus() {
        val article = ArticleDoc(
            sourceUrl = "https://kuvarnica.rs/pasulj",
            title = "Recipe for prebranac",
            textSr = "Ovo je jednostavan recipe for prebranac. cooking sa lukom.",
            excerpt = ""
        )
        val ranking = subject.classifyArticle(article)
        assertEquals("food", ranking.first())
    }

    @Test fun articleWithNoMatchesReturnsEmpty() {
        val article = ArticleDoc(
            sourceUrl = "x",
            title = "obscure title",
            textSr = "nothing relevant here",
            excerpt = ""
        )
        assertTrue(subject.classifyArticle(article).isEmpty())
    }
}
