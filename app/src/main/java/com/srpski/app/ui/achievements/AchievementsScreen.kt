package com.srpski.app.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.SrpskiApp
import com.srpski.app.domain.Achievements

@Composable
fun AchievementsScreen(app: SrpskiApp, onExit: () -> Unit) {
    var stats by remember { mutableStateOf<Achievements.Stats?>(null) }

    LaunchedEffect(Unit) {
        stats = Achievements.Stats(
            lessonsCompleted = app.progress.completedLessonCount(),
            currentStreak = app.progress.currentStreak(),
            totalXp = app.progress.totalXp(),
            correctAnswers = app.progress.totalCorrectAnswers(),
            sharedItems = app.sharedMedia.count()
        )
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trophies", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onExit) { Text("Done") }
        }
        Spacer(Modifier.height(16.dp))
        val current = stats
        if (current == null) {
            Text("Loading…")
            return@Column
        }
        val progress = Achievements.evaluate(current)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(progress) { p -> AchievementCard(p) }
        }
    }
}

@Composable
private fun AchievementCard(p: Achievements.Progress) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    p.definition.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (p.unlocked) "Unlocked" else "${p.current}/${p.definition.threshold}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(p.definition.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { p.fraction },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
