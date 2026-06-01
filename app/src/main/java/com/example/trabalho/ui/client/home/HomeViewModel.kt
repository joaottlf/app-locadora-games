package com.example.trabalho.ui.client.home

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
import kotlinx.coroutines.flow.combine // <-- NOVO IMPORT
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _userName = MutableStateFlow("Carregando...")
    val userName: StateFlow<String> = _userName

    val gameList: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchUserName()
    }

    val trendingGames: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .map { listaDeJogos ->
            listaDeJogos.filter { jogo -> jogo.isTrending }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val newReleaseGames: StateFlow<List<Game>> = gameRepository.getGamesRealtime()
        .map { listaDeJogos ->
            listaDeJogos.filter { jogo -> jogo.isNewRelease }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- NOVA LÓGICA DE PESQUISA ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Atualiza o texto digitado
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Combina o que foi digitado com a lista total de jogos
    val searchResults: StateFlow<List<Game>> = combine(gameList, _searchQuery) { games, query ->
        if (query.isBlank()) {
            emptyList() // Se não digitou nada, não mostra resultados de pesquisa
        } else {
            // Ignora maiúsculas e minúsculas ao pesquisar
            games.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    // --------------------------------

    private fun fetchUserName() {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid != null) {
            userRepository.getUserName(uid) { name ->
                _userName.value = name
            }
        } else {
            _userName.value = "Visitante"
        }
    }
}