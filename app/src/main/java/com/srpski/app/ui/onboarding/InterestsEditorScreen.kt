package com.srpski.app.ui.onboarding

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.SrpskiApp
import com.srpski.app.data.model.TopicDef
import com.srpski.app.domain.InterestUpdater
import kotlinx.coroutines.launch

/**
 * Settings → "What I like" viewer. Shows every topic with its current weight
 * and lets the user boost or reset a topic. Interest weights also update
 * implicitly from shared media; this screen is the manual escape hatch.
 */
@Composable
fun InterestsEditorScreen(app: SrpskiApp, onExit: () -> Unit) {
    val weights by app.interests.observeAll().collectAsState(initial = emptyMap())
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("What I like", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onExit) { Text("Done") }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Boost a topic to see more of it; sharing a matching clip does this automatically.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(app.topicMap.topics) { topic ->
                InterestRow(topic, weights[topic.id] ?: InterestUpdater.MIN_WEIGHT) {
                    scope.launch { app.interests.reinforce(listOf(topic.id)) }
                }
            }
        }
    }
}

@Composable
private fun InterestRow(topic: TopicDef, weight: Double, onBoost: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(topic.label, style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onBoost) { Text("Boost") }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { weight.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
