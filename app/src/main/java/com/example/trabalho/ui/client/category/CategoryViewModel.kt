package com.example.trabalho.ui.client.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.model.Game
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CategoryViewModel(
    gameRepository: GameRepository,
    categoryName: String
) : ViewModel() {

    // Filtra diretamente a stream de jogos pela categoria escolhida
    val categoryGames: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .map { games ->
            games.filter { it.category.equals(categoryName, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}