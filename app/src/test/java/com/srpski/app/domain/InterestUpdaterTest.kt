package com.srpski.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InterestUpdaterTest {

    @Test fun reinforceMovesTowardOne() {
        var w = 0.05
        repeat(50) { w = InterestUpdater.reinforce(w) }
        assertTrue("expected weight near 1.0 after 50 reinforcements, got $w", w > 0.99)
    }

    @Test fun reinforceIsBoundedByMax() {
        val w = InterestUpdater.reinforce(1.0)
        assertEquals(1.0, w, 1e-9)
    }

    @Test fun decayFloorsAtMin() {
        var w = 1.0
        repeat(500) { w = InterestUpdater.decay(w) }
        assertEquals(InterestUpdater.MIN_WEIGHT, w, 1e-9)
    }

    @Test fun singleReinforceMovesByAlphaFractionOfDelta() {
        val start = 0.5
        val step = InterestUpdater.reinforce(start)
        // step = start + ALPHA * (1 - start) = 0.5 + 0.1 * 0.5 = 0.55
        assertEquals(0.55, step, 1e-9)
    }

    @Test fun pickedSeedIsOne() {
        assertEquals(1.0, InterestUpdater.pickedSeed(), 1e-9)
    }

    @Test fun unpickedSeedIsMin() {
        assertEquals(InterestUpdater.MIN_WEIGHT, InterestUpdater.unpickedSeed(), 1e-9)
    }
}
