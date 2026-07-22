package com.srpski.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.data.model.TopicDef

const val MIN_ONBOARDING_TOPICS = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    topics: List<TopicDef>,
    onFinish: (Set<String>) -> Unit
) {
    var picked by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "What are you into?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Pick at least $MIN_ONBOARDING_TOPICS. We'll surface real Serbian clips " +
                "and articles that match — nothing leaves your phone.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topics.forEach { topic ->
                val selected = topic.id in picked
                FilterChip(
                    selected = selected,
                    onClick = {
                        picked = if (selected) picked - topic.id else picked + topic.id
                    },
                    label = { Text(topic.label) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "${picked.size}/$MIN_ONBOARDING_TOPICS selected",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = picked.size >= MIN_ONBOARDING_TOPICS,
            onClick = { onFinish(picked) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start learning") }
    }
}
