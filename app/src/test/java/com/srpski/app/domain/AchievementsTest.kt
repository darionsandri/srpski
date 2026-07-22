package com.srpski.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {

    @Test fun firstStepsUnlocksOnFirstLesson() {
        val progress = Achievements.evaluate(
            Achievements.Stats(lessonsCompleted = 1, currentStreak = 0, totalXp = 0, correctAnswers = 0, sharedItems = 0)
        )
        val firstSteps = progress.first { it.definition.id == "first_steps" }
        assertTrue(firstSteps.unlocked)
    }

    @Test fun streakAchievementScalesByStreakCount() {
        val progress = Achievements.evaluate(
            Achievements.Stats(lessonsCompleted = 0, currentStreak = 8, totalXp = 0, correctAnswers = 0, sharedItems = 0)
        )
        assertTrue(progress.first { it.definition.id == "streak_7" }.unlocked)
        assertFalse(progress.first { it.definition.id == "streak_30" }.unlocked)
    }

    @Test fun mediaCountUnlocksSharesAchievement() {
        val progress = Achievements.evaluate(
            Achievements.Stats(lessonsCompleted = 0, currentStreak = 0, totalXp = 0, correctAnswers = 0, sharedItems = 1)
        )
        assertTrue(progress.first { it.definition.id == "media_curious" }.unlocked)
        assertFalse(progress.first { it.definition.id == "media_enthusiast" }.unlocked)
    }

    @Test fun fractionClampedAtOne() {
        val progress = Achievements.evaluate(
            Achievements.Stats(lessonsCompleted = 100, currentStreak = 0, totalXp = 0, correctAnswers = 0, sharedItems = 0)
        )
        assertEquals(1f, progress.first { it.definition.id == "commuter" }.fraction, 1e-6f)
    }
}
