package com.srpski.app.ui.packs

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srpski.app.SrpskiApp
import com.srpski.app.data.ContentSync
import kotlinx.coroutines.launch

@Composable
fun PacksScreen(app: SrpskiApp, onExit: () -> Unit) {
    var available by remember { mutableStateOf<List<ContentSync.AvailablePack>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        available = app.contentSync.refreshIndex()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Content packs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onExit) { Text("Done") }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Optional lesson bundles hosted on the Srpski content site. Nothing here is required to learn.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        if (busy) CircularProgressIndicator()
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        val list = available
        when {
            list == null -> Text("Checking…")
            list.isEmpty() -> Text("No packs available yet.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list) { entry ->
                    PackCard(entry, busy) { action ->
                        busy = true; error = null
                        scope.launch {
                            when (action) {
                                is PackAction.Install -> app.contentSync.install(entry.summary)
                                    .onFailure { error = it.message }
                                PackAction.Uninstall -> app.contentSync.uninstall(entry.summary.id)
                            }
                            available = app.contentSync.refreshIndex()
                            busy = false
                        }
                    }
                }
            }
        }
    }
}

private sealed interface PackAction {
    data object Install : PackAction
    data object Uninstall : PackAction
}

@Composable
private fun PackCard(entry: ContentSync.AvailablePack, busy: Boolean, onAction: (PackAction) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(entry.summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${entry.summary.cefr}  ·  v${entry.summary.version}  ·  ${bytesLabel(entry.summary.sizeBytes)}",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val installed = entry.installedVersion
                if (installed == null) {
                    Button(enabled = !busy, onClick = { onAction(PackAction.Install) }) { Text("Install") }
                } else {
                    if (installed < entry.summary.version) {
                        Button(enabled = !busy, onClick = { onAction(PackAction.Install) }) { Text("Update") }
                    } else {
                        Text("Installed", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(enabled = !busy, onClick = { onAction(PackAction.Uninstall) }) { Text("Remove") }
                }
            }
        }
    }
}

private fun bytesLabel(b: Long): String = when {
    b > 1_048_576 -> "%.1f MB".format(b / 1_048_576.0)
    b > 1024 -> "%.0f KB".format(b / 1024.0)
    else -> "$b B"
}
