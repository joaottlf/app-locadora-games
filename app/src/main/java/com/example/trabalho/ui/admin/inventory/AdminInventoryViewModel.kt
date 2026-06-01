package com.example.trabalho.ui.admin.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdminInventoryViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    val gameList: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun saveGame(game: Game) {
        _isLoading.value = true
        _feedbackMessage.value = null

        if (game.id.isBlank()) {
            gameRepository.addGame(game) { success, error ->
                _isLoading.value = false
                if (success) _feedbackMessage.value = "Jogo adicionado com sucesso!"
                else _feedbackMessage.value = "Erro ao adicionar: $error"
            }
        } else {
            gameRepository.updateGame(game) { success, error ->
                _isLoading.value = false
                if (success) _feedbackMessage.value = "Jogo atualizado com sucesso!"
                else _feedbackMessage.value = "Erro ao atualizar: $error"
            }
        }
    }

    fun deleteGame(gameId: String) {
        _isLoading.value = true
        _feedbackMessage.value = null

        gameRepository.deleteGame(gameId) { success, error ->
            _isLoading.value = false
            if (success) _feedbackMessage.value = "Jogo excluído."
            else _feedbackMessage.value = "Erro ao excluir: $error"
        }
    }
}