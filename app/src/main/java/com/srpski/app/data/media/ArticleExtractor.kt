package com.srpski.app.data.media

import com.srpski.app.data.model.ArticleDoc
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Lightweight Readability-style extractor. No dependencies beyond jsoup.
 * Picks the deepest element with the largest text density (chars / tags)
 * and strips scripts, nav, header, footer, aside, form. Good enough as an
 * MVP for Serbian news sites; can be replaced by a proper Readability port
 * later without changing the [ArticleDoc] output shape.
 */
object ArticleExtractor {

    private val BAD_TAGS = setOf(
        "script", "style", "nav", "header", "footer", "aside", "form",
        "noscript", "svg", "iframe", "figure", "figcaption"
    )

    fun extract(sourceUrl: String, html: String): ArticleDoc {
        val doc = Jsoup.parse(html, sourceUrl)
        val title = pickTitle(doc)
        val byline = pickByline(doc)
        val root = pickContent(doc)
        val text = root?.let(::collectText).orEmpty()
        val excerpt = text.take(280).replace(Regex("\\s+"), " ").trim()
        return ArticleDoc(
            sourceUrl = sourceUrl,
            title = title,
            byline = byline,
            language = doc.select("html").attr("lang").ifBlank { "sr" },
            textSr = text,
            excerpt = excerpt
        )
    }

    private fun pickTitle(doc: Document): String {
        val og = doc.select("meta[property=og:title]").attr("content").trim()
        if (og.isNotBlank()) return og
        val h1 = doc.selectFirst("h1")?.text()?.trim().orEmpty()
        if (h1.isNotBlank()) return h1
        return doc.title().trim()
    }

    private fun pickByline(doc: Document): String? {
        val og = doc.select("meta[name=author]").attr("content").trim()
        if (og.isNotBlank()) return og
        val rel = doc.selectFirst("[rel=author]")?.text()?.trim()
        return rel?.takeIf { it.isNotBlank() }
    }

    private fun pickContent(doc: Document): Element? {
        // Prefer explicit semantic containers.
        doc.selectFirst("article")?.let { return it }
        doc.selectFirst("main")?.let { return it }
        // Otherwise pick the container div with the highest text density.
        val candidates = doc.body().select("div, section")
        return candidates.maxByOrNull { textDensity(it) }
    }

    private fun textDensity(el: Element): Double {
        val text = el.text().length
        val descendants = el.allElements.size.coerceAtLeast(1)
        return text.toDouble() / descendants
    }

    private fun collectText(root: Element): String {
        // Walk children; keep <p>, <li>, <h*> text, drop everything in BAD_TAGS.
        val sb = StringBuilder()
        for (child in root.getAllElements()) {
            val tag = child.tagName().lowercase()
            if (tag in BAD_TAGS) continue
            if (tag !in setOf("p", "li", "h1", "h2", "h3", "h4", "blockquote")) continue
            val t = child.ownText().trim()
            if (t.isNotBlank()) {
                sb.append(t).append('\n')
            }
        }
        return sb.toString().trim()
    }
}
