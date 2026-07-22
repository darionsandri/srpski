package com.srpski.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.data.model.ExerciseDef
import com.srpski.app.domain.GradeResult
import com.srpski.app.ui.media.YouTubeClipPlayer
import com.srpski.app.ui.theme.CorrectGreen
import com.srpski.app.ui.theme.WrongRed

@Composable
fun LessonScreen(viewModel: LessonViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsState()

    if (state.finished) {
        LessonFinished(sessionXp = state.sessionXp, onExit = onExit)
        return
    }
    val exercise = state.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        // key() resets per-exercise input state when the exercise changes
        androidx.compose.runtime.key(exercise.id, state.index) {
            when (exercise.type) {
                "multipleChoice", "fillBlank" -> ChoiceExercise(exercise, state, viewModel)
                "wordBank" -> WordBankExercise(exercise, state, viewModel)
                "typing" -> TypingExercise(exercise, state, viewModel)
                "listening" -> ListeningExercise(exercise, state, viewModel)
                "videoTranslate" -> VideoTranslateExercise(exercise, state, viewModel)
                "videoGist" -> VideoGistExercise(exercise, state, viewModel)
                "videoCloze" -> VideoClozeExercise(exercise, state, viewModel)
                else -> Text("Unsupported exercise type: ${exercise.type}")
            }
        }

        Spacer(Modifier.height(24.dp))
        state.feedback?.let { FeedbackPanel(it, onNext = viewModel::next) }
    }
}

@Composable
private fun Prompt(exercise: ExerciseDef, viewModel: LessonViewModel) {
    val label = when {
        exercise.sentence != null -> "Complete the sentence"
        exercise.promptSr != null -> "Translate to English"
        else -> "Translate to Serbian"
    }
    Text(label, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(8.dp))
    val text = exercise.sentence ?: exercise.promptSr ?: exercise.promptEn ?: ""
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    if (exercise.promptSr != null) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.speak(exercise.promptSr) }) { Text("Play audio") }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun ChoiceExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    Prompt(exercise, viewModel)
    exercise.options.forEachIndexed { i, option ->
        OutlinedButton(
            onClick = { if (state.feedback == null) viewModel.submitChoice(i) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) { Text(option) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordBankExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    var chosen by remember { mutableStateOf(listOf<Int>()) }
    Prompt(exercise, viewModel)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Text(
            chosen.joinToString(" ") { exercise.tiles[it] },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
    Spacer(Modifier.height(16.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        exercise.tiles.forEachIndexed { i, tile ->
            val used = i in chosen
            OutlinedButton(
                enabled = !used && state.feedback == null,
                onClick = { chosen = chosen + i }
            ) { Text(tile) }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            enabled = chosen.isNotEmpty() && state.feedback == null,
            onClick = { chosen = chosen.dropLast(1) }
        ) { Text("Undo") }
        Button(
            enabled = chosen.isNotEmpty() && state.feedback == null,
            onClick = { viewModel.submitText(chosen.joinToString(" ") { exercise.tiles[it] }) }
        ) { Text("Check") }
    }
}

@Composable
private fun TypingExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    var input by remember { mutableStateOf("") }
    Prompt(exercise, viewModel)
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Type in Serbian") },
        enabled = state.feedback == null
    )
    Spacer(Modifier.height(12.dp))
    Button(
        enabled = input.isNotBlank() && state.feedback == null,
        onClick = { viewModel.submitText(input) }
    ) { Text("Check") }
}

@Composable
private fun ListeningExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    var input by remember { mutableStateOf("") }
    Text("Type what you hear", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { exercise.audioText?.let { viewModel.speak(it) } }) { Text("Play") }
        OutlinedButton(onClick = { exercise.audioText?.let { viewModel.speak(it, slow = true) } }) { Text("Slow") }
    }
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Type in Serbian") },
        enabled = state.feedback == null
    )
    Spacer(Modifier.height(12.dp))
    Button(
        enabled = input.isNotBlank() && state.feedback == null,
        onClick = { viewModel.submitText(input) }
    ) { Text("Check") }
}

@Composable
private fun VideoTranslateExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    val videoId = exercise.youtubeId
    if (videoId == null) { Text("Missing video reference"); return }
    Text("Translate what you hear in the clip", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(12.dp))
    YouTubeClipPlayer(videoId = videoId, startSec = exercise.startSec, endSec = exercise.endSec)
    Spacer(Modifier.height(16.dp))
    var input by remember { mutableStateOf("") }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Type in Serbian") },
        enabled = state.feedback == null
    )
    Spacer(Modifier.height(12.dp))
    Button(
        enabled = input.isNotBlank() && state.feedback == null,
        onClick = { viewModel.submitText(input) }
    ) { Text("Check") }
}

@Composable
private fun VideoGistExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    val videoId = exercise.youtubeId
    if (videoId == null) { Text("Missing video reference"); return }
    Text(exercise.promptEn ?: "What is this clip about?", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(12.dp))
    YouTubeClipPlayer(videoId = videoId, startSec = exercise.startSec, endSec = exercise.endSec)
    Spacer(Modifier.height(16.dp))
    exercise.options.forEachIndexed { i, option ->
        OutlinedButton(
            onClick = { if (state.feedback == null) viewModel.submitChoice(i) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) { Text(option) }
    }
}

@Composable
private fun VideoClozeExercise(exercise: ExerciseDef, state: LessonUiState, viewModel: LessonViewModel) {
    val videoId = exercise.youtubeId
    if (videoId == null) { Text("Missing video reference"); return }
    Text("Fill in the missing word", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(12.dp))
    YouTubeClipPlayer(videoId = videoId, startSec = exercise.startSec, endSec = exercise.endSec)
    Spacer(Modifier.height(16.dp))
    val transcript = exercise.transcriptSr ?: exercise.sentence.orEmpty()
    if (transcript.isNotBlank()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                transcript,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(12.dp))
    }
    var input by remember { mutableStateOf("") }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Missing word") },
        enabled = state.feedback == null
    )
    Spacer(Modifier.height(12.dp))
    Button(
        enabled = input.isNotBlank() && state.feedback == null,
        onClick = { viewModel.submitText(input) }
    ) { Text("Check") }
}

@Composable
private fun FeedbackPanel(feedback: Feedback, onNext: () -> Unit) {
    val (color, headline) = when (feedback.result) {
        GradeResult.CORRECT -> CorrectGreen to "Correct!"
        GradeResult.CORRECT_TYPO -> CorrectGreen to "Correct — watch the small typo."
        GradeResult.CORRECT_ACCENTS -> CorrectGreen to "Correct — pay attention to the accents."
        GradeResult.WRONG ->
            WrongRed to if (feedback.accentHint) "Almost — the accents matter here." else "Not quite."
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(headline, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Answer: ${feedback.correctAnswer}")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNext) { Text("Continue") }
        }
    }
}

@Composable
private fun LessonFinished(sessionXp: Int, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bravo!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("+$sessionXp XP")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExit) { Text("Back to lessons") }
    }
}
