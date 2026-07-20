package com.srpski.app.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.SrpskiApp

@Composable
fun SkillTreeScreen(app: SrpskiApp, onOpenLesson: (String) -> Unit) {
    val progress by app.progress.lessonProgressFlow().collectAsState(initial = emptyList())
    val completedIds = progress.filter { it.completedCount > 0 }.map { it.lessonId }.toSet()

    var streak by remember { mutableIntStateOf(0) }
    var todayXp by remember { mutableIntStateOf(0) }
    var strict by remember { mutableStateOf(app.strictDiacritics) }
    LaunchedEffect(progress) {
        streak = app.progress.currentStreak()
        todayXp = app.progress.todayXp()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Srpski", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Streak $streak  ·  $todayXp XP today", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Strict accents", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Switch(checked = strict, onCheckedChange = {
                strict = it
                app.strictDiacritics = it
            })
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(app.content.course.units) { unit ->
                val unlocked = unit.requiresUnit == null ||
                    app.content.lessonsForUnit(unit.requiresUnit).all { it.id in completedIds }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${unit.title}  ·  ${unit.cefr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(unit.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        app.content.lessonsForUnit(unit.id).forEach { lesson ->
                            val done = lesson.id in completedIds
                            OutlinedButton(
                                enabled = unlocked,
                                onClick = { onOpenLesson(lesson.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(if (done) "${lesson.title}  ✓" else lesson.title)
                            }
                        }
                        if (!unlocked) {
                            Text(
                                "Finish the previous unit to unlock",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
