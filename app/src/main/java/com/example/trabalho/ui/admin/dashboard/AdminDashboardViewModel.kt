package com.example.trabalho.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.UserRepository
import com.example.trabalho.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdminDashboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _adminName = MutableStateFlow("Carregando...")
    val adminName: StateFlow<String> = _adminName

    val totalGamesCount: StateFlow<Int> = gameRepository.getGamesRealtime()
        .map { listaDeJogos -> listaDeJogos.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val lowStockGames: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .map { listaDeJogos -> listaDeJogos.filter { it.stockQuantity < 3 } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadAdminName()
    }

    private fun loadAdminName() {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid != null) {
            userRepository.getUserName(uid) { name ->
                _adminName.value = name
            }
        }
    }
}