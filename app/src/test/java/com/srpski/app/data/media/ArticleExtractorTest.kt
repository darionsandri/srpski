package com.srpski.app.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractorTest {

    @Test fun extractsTitleFromOgTag() {
        val html = """
            <html><head><meta property="og:title" content="Naslov"/><title>Fallback</title></head>
            <body><article><h1>Naslov</h1><p>Ovo je sadržaj članka na srpskom.</p></article></body></html>
        """.trimIndent()
        val doc = ArticleExtractor.extract("https://example.rs/x", html)
        assertEquals("Naslov", doc.title)
        assertTrue("expected article body extracted, got '${doc.textSr}'",
            doc.textSr.contains("Ovo je sadržaj"))
    }

    @Test fun fallsBackToTitleTagWhenNoOg() {
        val html = "<html><head><title>Vesti</title></head><body><p>Prva paragraf.</p></body></html>"
        val doc = ArticleExtractor.extract("https://example.rs/x", html)
        assertEquals("Vesti", doc.title)
    }

    @Test fun stripsScriptAndNav() {
        val html = """
            <html><body>
              <nav>Menu</nav>
              <script>alert(1)</script>
              <article><p>Sadržaj koji ostaje.</p></article>
              <footer>Podnožje</footer>
            </body></html>
        """.trimIndent()
        val doc = ArticleExtractor.extract("https://example.rs/x", html)
        assertTrue("expected Sadržaj, got '${doc.textSr}'", doc.textSr.contains("Sadržaj koji ostaje"))
        assertTrue("expected no Menu, got '${doc.textSr}'", !doc.textSr.contains("Menu"))
        assertTrue("expected no Podnožje, got '${doc.textSr}'", !doc.textSr.contains("Podnožje"))
    }
}
