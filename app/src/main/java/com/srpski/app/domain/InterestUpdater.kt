package com.srpski.app.domain

/**
 * Pure math for the interest-weight EMA and decay. No Android, no DB — testable on JVM.
 *
 * Weights live in [MIN_WEIGHT, MAX_WEIGHT]. A "hit" moves the weight toward 1.0
 * at [ALPHA]; a decay step multiplies the weight by [DECAY_PER_STEP]. Both are
 * floored at [MIN_WEIGHT] so no topic is permanently killed and the user can
 * always recover by sharing a matching item.
 */
object InterestUpdater {
    const val MIN_WEIGHT = 0.05
    const val MAX_WEIGHT = 1.0
    const val ALPHA = 0.10
    const val DECAY_PER_STEP = 0.98

    /** Reinforce a topic. Idempotent-ish; each call moves 10% closer to 1.0. */
    fun reinforce(current: Double): Double =
        clamp(current * (1.0 - ALPHA) + ALPHA * MAX_WEIGHT)

    /** Decay a topic that wasn't seen this cycle. */
    fun decay(current: Double): Double = clamp(current * DECAY_PER_STEP)

    /** Seed value for an onboarding-picked topic. */
    fun pickedSeed(): Double = MAX_WEIGHT

    /** Seed value for an onboarding-unpicked topic (floor). */
    fun unpickedSeed(): Double = MIN_WEIGHT

    private fun clamp(v: Double): Double = v.coerceIn(MIN_WEIGHT, MAX_WEIGHT)
}
