package com.srpski.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srpski.app.SrpskiApp
import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.domain.GradeResult
import com.srpski.app.domain.Grader
import com.srpski.app.domain.SessionBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Feedback(
    val result: GradeResult,
    val correctAnswer: String,
    val accentHint: Boolean = false
)

data class LessonUiState(
    val loading: Boolean = true,
    val exercises: List<ExerciseDef> = emptyList(),
    val index: Int = 0,
    val feedback: Feedback? = null,
    val sessionXp: Int = 0,
    val finished: Boolean = false
) {
    val current: ExerciseDef? get() = exercises.getOrNull(index)
    val progress: Float get() = if (exercises.isEmpty()) 0f else index.toFloat() / exercises.size
}

class LessonViewModel(
    private val app: SrpskiApp,
    private val lessonId: String
) : ViewModel() {

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state

    // Exercises answered wrong get one retry at the end of the session.
    private val retryQueue = mutableListOf<ExerciseDef>()
    private val retried = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            val lesson = app.content.lesson(lessonId)
            val dueVocab = app.progress.dueVocabIds(SessionBuilder.MAX_REVIEWS)
                .mapNotNull { app.content.vocabById[it] }
            val session = SessionBuilder.build(
                lessonExercises = lesson?.exercises ?: emptyList(),
                dueVocab = dueVocab,
                vocabPool = app.content.vocabById.values.toList()
            )
            _state.value = LessonUiState(loading = false, exercises = session)
        }
    }

    fun speak(text: String, slow: Boolean = false) = app.tts.speak(text, slow)

    fun submitChoice(selectedIndex: Int) {
        val ex = _state.value.current ?: return
        val correct = selectedIndex == ex.correctIndex
        val answerText = ex.options.getOrNull(ex.correctIndex ?: -1) ?: ""
        finishGrading(ex, if (correct) GradeResult.CORRECT else GradeResult.WRONG, answerText)
    }

    fun submitText(input: String) {
        val ex = _state.value.current ?: return
        val result = Grader.grade(input, ex.accepted, strictDiacritics(ex))
        val accentHint = result == GradeResult.WRONG && Grader.isAccentOnlyMiss(input, ex.accepted)
        finishGrading(ex, result, ex.accepted.firstOrNull() ?: "", accentHint)
    }

    private fun strictDiacritics(ex: ExerciseDef): Boolean =
        app.strictDiacritics && ex.diacriticsSensitive

    private fun finishGrading(
        ex: ExerciseDef,
        result: GradeResult,
        correctAnswer: String,
        accentHint: Boolean = false
    ) {
        val correctEnough = result != GradeResult.WRONG
        val xp = when (result) {
            GradeResult.CORRECT -> 10
            GradeResult.CORRECT_ACCENTS, GradeResult.CORRECT_TYPO -> 5
            GradeResult.WRONG -> 0
        }
        if (!correctEnough && ex.id !in retried) {
            retryQueue += ex
            retried += ex.id
        }
        viewModelScope.launch {
            ex.vocabRefs.forEach { app.progress.recordVocabAnswer(it, correctEnough) }
            if (xp > 0) app.progress.addXp(xp)
        }
        _state.value = _state.value.copy(
            feedback = Feedback(result, correctAnswer, accentHint),
            sessionXp = _state.value.sessionXp + xp
        )
    }

    fun next() {
        val s = _state.value
        val nextIndex = s.index + 1
        if (nextIndex < s.exercises.size) {
            _state.value = s.copy(index = nextIndex, feedback = null)
            return
        }
        if (retryQueue.isNotEmpty()) {
            val retries = retryQueue.toList()
            retryQueue.clear()
            _state.value = s.copy(
                exercises = s.exercises + retries,
                index = nextIndex,
                feedback = null
            )
            return
        }
        viewModelScope.launch { app.progress.recordLessonComplete(lessonId) }
        _state.value = s.copy(finished = true, feedback = null)
    }
}
