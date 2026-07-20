package com.srpski.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GraderTest {

    private val acceptedCetiri = listOf("četiri")
    private val acceptedDobarDan = listOf("dobar dan")

    @Test fun exactMatchIsCorrect() {
        assertEquals(GradeResult.CORRECT, Grader.grade("dobar dan", acceptedDobarDan, true))
    }

    @Test fun normalizationHandlesCaseSpacingPunctuation() {
        assertEquals(GradeResult.CORRECT, Grader.grade("  Dobar   dan!  ", acceptedDobarDan, true))
    }

    @Test fun strictModeRejectsMissingDiacritics() {
        assertEquals(GradeResult.WRONG, Grader.grade("cetiri", acceptedCetiri, true))
        assertTrue(Grader.isAccentOnlyMiss("cetiri", acceptedCetiri))
    }

    @Test fun lenientModeAcceptsMissingDiacriticsWithFlag() {
        assertEquals(GradeResult.CORRECT_ACCENTS, Grader.grade("cetiri", acceptedCetiri, false))
    }

    @Test fun djDigraphMatchesDjeLetter() {
        assertEquals(GradeResult.CORRECT_ACCENTS, Grader.grade("dovidjenja", listOf("doviđenja"), false))
    }

    @Test fun typoToleranceOnLongerWords() {
        assertEquals(GradeResult.CORRECT_TYPO, Grader.grade("polakko", listOf("polako"), true))
    }

    @Test fun noTypoToleranceOnShortWords() {
        assertEquals(GradeResult.WRONG, Grader.grade("da", listOf("ne"), true))
        assertEquals(GradeResult.WRONG, Grader.grade("dan", listOf("da"), true))
    }

    @Test fun wrongIsWrong() {
        assertEquals(GradeResult.WRONG, Grader.grade("hvala", acceptedDobarDan, true))
    }

    @Test fun emptyInputIsWrong() {
        assertEquals(GradeResult.WRONG, Grader.grade("   ", acceptedDobarDan, false))
    }
}
