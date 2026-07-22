package com.srpski.app.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.data.db.SharedMedium

@Composable
fun ShareIngestScreen(
    viewModel: ShareIngestViewModel,
    rawText: String,
    onExit: () -> Unit
) {
    LaunchedEffect(rawText) { viewModel.start(rawText) }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val s = state) {
            ShareIngestUiState.Idle, ShareIngestUiState.Working -> WorkingCard()
            is ShareIngestUiState.Ready -> ReadyCard(s.medium, s.topicIds, onExit)
            is ShareIngestUiState.Failed -> FailedCard(s.reason, onExit)
        }
    }
}

@Composable
private fun WorkingCard() {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text("Adding to your library…")
}

@Composable
private fun ReadyCard(medium: SharedMedium, topicIds: List<String>, onExit: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                when (medium.kind) {
                    SharedMedium.KIND_YOUTUBE -> "YouTube clip added"
                    SharedMedium.KIND_ARTICLE -> "Article saved"
                    else -> "Text saved"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(medium.title ?: medium.url, style = MaterialTheme.typography.bodyMedium)
            if (topicIds.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Tagged: " + topicIds.joinToString(", "),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExit) { Text("Save for later") }
            }
        }
    }
}

@Composable
private fun FailedCard(reason: String, onExit: () -> Unit) {
    Text("Couldn't add", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Text(reason)
    Spacer(Modifier.height(20.dp))
    OutlinedButton(onClick = onExit) { Text("Close") }
}
