package com.srpski.app.domain

import com.srpski.app.data.db.DailyStat

object Streaks {
    /** Current streak in days, counting back from today (or yesterday if today has no XP yet). */
    fun currentStreak(recentDaysDesc: List<DailyStat>, todayEpochDay: Long): Int {
        val active = recentDaysDesc.filter { it.xp > 0 }.map { it.epochDay }.toSet()
        var day = if (todayEpochDay in active) todayEpochDay else todayEpochDay - 1
        var streak = 0
        while (day in active) {
            streak++
            day--
        }
        return streak
    }
}
