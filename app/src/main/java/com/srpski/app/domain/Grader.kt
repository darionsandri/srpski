package com.srpski.app.domain

/** Result of grading a typed answer. */
enum class GradeResult {
    CORRECT,            // exact match after normalization
    CORRECT_ACCENTS,    // right except diacritics (lenient mode only)
    CORRECT_TYPO,       // one-letter slip on a longer answer
    WRONG
}

/**
 * Grades typed answers. Pure Kotlin, no Android dependencies.
 *
 * Normalization: lowercase, trim, collapse whitespace, strip terminal punctuation.
 * Strict mode: "cetiri" != "četiri" -> WRONG (with an accent hint shown by the UI).
 * Lenient mode: "cetiri" == "četiri" -> CORRECT_ACCENTS with a correction banner.
 * Typo tolerance: edit distance 1 accepted when the target is >= 5 characters.
 */
object Grader {

    fun grade(
        input: String,
        accepted: List<String>,
        strictDiacritics: Boolean
    ): GradeResult {
        val normIn = normalize(input)
        if (normIn.isEmpty()) return GradeResult.WRONG
        val normAccepted = accepted.map { normalize(it) }

        if (normIn in normAccepted) return GradeResult.CORRECT

        val strippedIn = stripDiacritics(normIn)
        val accentMatch = normAccepted.any { stripDiacritics(it) == strippedIn }
        if (accentMatch) {
            return if (strictDiacritics) GradeResult.WRONG else GradeResult.CORRECT_ACCENTS
        }

        val typoMatch = normAccepted.any { target ->
            target.length >= 5 && levenshtein(normIn, target) == 1
        }
        if (typoMatch) return GradeResult.CORRECT_TYPO

        return GradeResult.WRONG
    }

    /** True when the miss was only about diacritics — used for the accent hint in strict mode. */
    fun isAccentOnlyMiss(input: String, accepted: List<String>): Boolean {
        val normIn = normalize(input)
        return normIn !in accepted.map { normalize(it) } &&
            accepted.any { stripDiacritics(normalize(it)) == stripDiacritics(normIn) }
    }

    fun normalize(s: String): String =
        s.lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .trimEnd('.', ',', '!', '?')
            .trim()

    fun stripDiacritics(s: String): String =
        buildString {
            for (c in s) {
                when (c) {
                    'č', 'ć' -> append('c')
                    'š' -> append('s')
                    'ž' -> append('z')
                    'đ' -> append("dj")
                    else -> append(c)
                }
            }
        }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val tmp = dp[j]
                dp[j] = minOf(
                    dp[j] + 1,
                    dp[j - 1] + 1,
                    prev + if (a[i - 1] == b[j - 1]) 0 else 1
                )
                prev = tmp
            }
        }
        return dp[b.length]
    }
}
