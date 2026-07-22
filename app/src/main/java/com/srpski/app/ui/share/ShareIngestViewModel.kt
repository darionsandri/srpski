package com.srpski.app.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srpski.app.SrpskiApp
import com.srpski.app.data.SharedMediaRepository
import com.srpski.app.data.db.SharedMedium
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ShareIngestUiState {
    data object Idle : ShareIngestUiState
    data object Working : ShareIngestUiState
    data class Ready(
        val medium: SharedMedium,
        val topicIds: List<String>
    ) : ShareIngestUiState
    data class Failed(val reason: String) : ShareIngestUiState
}

class ShareIngestViewModel(
    private val app: SrpskiApp,
    private val repository: SharedMediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ShareIngestUiState>(ShareIngestUiState.Idle)
    val state: StateFlow<ShareIngestUiState> = _state

    fun start(rawText: String) {
        if (_state.value !is ShareIngestUiState.Idle) return
        _state.value = ShareIngestUiState.Working
        viewModelScope.launch {
            val result = runCatching { repository.ingest(rawText) }
            _state.value = result.fold(
                onSuccess = { ShareIngestUiState.Ready(it.medium, it.detectedTopics) },
                onFailure = { ShareIngestUiState.Failed(it.message ?: "Unknown error") }
            )
        }
    }
}
