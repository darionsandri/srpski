package com.srpski.app.domain

/**
 * Pure achievement definitions + evaluator. Everything is derived from
 * numbers already tracked in Room (streak, total XP, lesson completions,
 * shared media count) — no dedicated storage.
 */
object Achievements {

    data class Definition(
        val id: String,
        val title: String,
        val description: String,
        val threshold: Int
    )

    data class Stats(
        val lessonsCompleted: Int,
        val currentStreak: Int,
        val totalXp: Int,
        val correctAnswers: Int,
        val sharedItems: Int
    )

    data class Progress(
        val definition: Definition,
        val current: Int
    ) {
        val unlocked: Boolean get() = current >= definition.threshold
        val fraction: Float get() =
            (current.toFloat() / definition.threshold).coerceIn(0f, 1f)
    }

    val definitions: List<Definition> = listOf(
        Definition("first_steps", "First steps", "Complete 1 lesson", 1),
        Definition("commuter", "Commuter", "Complete 10 lessons", 10),
        Definition("streak_7", "One-week streak", "Reach a 7-day streak", 7),
        Definition("streak_30", "One-month streak", "Reach a 30-day streak", 30),
        Definition("xp_500", "500 XP", "Earn 500 XP total", 500),
        Definition("xp_5000", "5,000 XP", "Earn 5,000 XP total", 5000),
        Definition("word_slayer", "Word slayer", "Answer 100 correct vocab prompts", 100),
        Definition("media_curious", "Media curious", "Share 1 item into Srpski", 1),
        Definition("media_enthusiast", "Media enthusiast", "Share 10 items into Srpski", 10)
    )

    fun evaluate(stats: Stats): List<Progress> = definitions.map { def ->
        val current = when (def.id) {
            "first_steps", "commuter" -> stats.lessonsCompleted
            "streak_7", "streak_30" -> stats.currentStreak
            "xp_500", "xp_5000" -> stats.totalXp
            "word_slayer" -> stats.correctAnswers
            "media_curious", "media_enthusiast" -> stats.sharedItems
            else -> 0
        }
        Progress(def, current)
    }
}
