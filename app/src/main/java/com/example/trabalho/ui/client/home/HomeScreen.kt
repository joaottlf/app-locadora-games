package com.example.trabalho.ui.client.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabalho.ui.components.CategoryCard
import com.example.trabalho.ui.components.GameItemCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: (String) -> Unit,
    onGameClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val trendingGames by viewModel.trendingGames.collectAsState()
    val newReleaseGames by viewModel.newReleaseGames.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allGames by viewModel.gameList.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- CABEÇALHO ---
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Olá, $userName!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Sua nova aventura espera por você aqui!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // --- BARRA DE PESQUISA ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar jogos pelo título...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        // --- LÓGICA DE EXIBIÇÃO ---
        if (searchQuery.isNotBlank()) {
            item { Text("Resultados da pesquisa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (searchResults.isEmpty()) {
                item { Text("Nenhum jogo encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(searchResults) { game ->
                    GameItemCard(game = game, onClick = { onGameClick(game.id) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else {
            // --- CATEGORIAS ---

            item {
                val categories = allGames
                    .map { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories) { category ->
                        CategoryCard(name = category, onClick = { onCategoryClick(category) })
                    }
                }
            }

            // --- NOVIDADES ---
            item {
                Text("Novidades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (newReleaseGames.isEmpty()) {
                    Text("Nenhum lançamento no momento.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(newReleaseGames) { game ->
                            Box(modifier = Modifier.fillParentMaxWidth(0.9f)) {
                                GameItemCard(game = game, onClick = { onGameClick(game.id) })
                            }
                        }
                    }
                }
            }

            // --- EM ALTA ---
            item {
                Text("Em alta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (trendingGames.isEmpty()) {
                    Text("Nenhum jogo em alta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(trendingGames) { game ->
                            Box(modifier = Modifier.fillParentMaxWidth(0.9f)) {
                                GameItemCard(game = game, onClick = { onGameClick(game.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}